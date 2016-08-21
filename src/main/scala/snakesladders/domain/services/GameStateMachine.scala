package snakesladders.domain.services

import akka.actor.FSM
import snakesladders.domain.events._
import snakesladders.domain.models.GameDefinitions._
import snakesladders.domain.models.GameState._
import snakesladders.domain.models.Players._
import snakesladders.domain.models._

class GameStateMachine extends FSM[GameState.State, Data] {

  import GameStateMachine._

  startWith(DeterminingPlayOrder, Uninitialized)

  when(DeterminingPlayOrder) {
    case Event(InitializeGame(game), Uninitialized) =>
      publishEvent(GameStarted)
      publishEventAndStay(CurrentPlayerChanged(game.players.head))
        .using(PlayOrderData(game))

    case Event(PlayerHasRolled(player, _), PlayOrderData(game, _, currentPlayer))
      if game.players(currentPlayer) != player => // it's not this players turn
      publishEventAndStay(WrongPlayerRolled(player))

    case Event(PlayerHasRolled(player, rolled), PlayOrderData(_, playOrder, _))
      if playOrder.exists(_.rolled == rolled) => // someone rolled this before and the player must roll again
      publishEventAndStay(PlayerMustRollAgain(player))

    case Event(PlayerHasRolled(player, rolled), PlayOrderData(game, playOrder, currentPlayer)) =>
      val newPlayOrder = (PlayOrder(player, rolled) +: playOrder).sorted

      if (game.players.length == newPlayOrder.length) {
        val positions = game.players.map(p => p -> Position(p, 1)).toMap
        publishEvent(PlayOrderDetermined(newPlayOrder))
        publishEventAndGoto(CurrentPlayerChanged(newPlayOrder.head.player), MainGame)
          .using(GameData(game, newPlayOrder, positions))
      } else {
        val next = nextPlayer(game, currentPlayer)
        publishEventAndStay(CurrentPlayerChanged(game.players(next)))
          .using(PlayOrderData(game, newPlayOrder, next))
      }
  }

  when(MainGame) {
    case Event(PlayerHasRolled(player, _), GameData(game, playOrder, _, currentPlayer, _))
      if playOrder(currentPlayer).player != player => // it's not this players turn
      publishEventAndStay(WrongPlayerRolled(player))

    case Event(PlayerHasRolled(player, rolled), data @ GameData(game, playOrder, positions, currentPlayer, _))
      if positions(player).position + rolled > game.gameDefinition.fieldCount => // not on the game board
      val next = nextPlayer(game, currentPlayer)
      publishEventAndStay(CurrentPlayerChanged(playOrder(next).player))
        .using(data.copy(currentPlayer = next))

    case Event(PlayerHasRolled(player, rolled), data @ GameData(game, playOrder, positions, currentPlayer, _))
      if positions(player).position + rolled == game.gameDefinition.fieldCount => // player has reached the last field
      val next = nextPlayer(game, currentPlayer)
      val finalPosition = Position(player, game.gameDefinition.fieldCount)
      publishEvent(PlayerMovedToPosition(finalPosition))
      publishEventAndGoto(GameIsOver(player), GameOver)
        .using(data.copy(positions = positions.updated(player, finalPosition), currentPlayer = next, winner = Some(player)))

    case Event(PlayerHasRolled(player, rolled), data @ GameData(game, playOrder, positions, currentPlayer, _)) =>
      val next = nextPlayer(game, currentPlayer)
      val newPosition = Position(player, positions(player).position + rolled)
      val actionField = game.gameDefinition.actionFields.find(_.from == newPosition.position)
      val finalPosition = actionField.map(af => Position(player, af.to)) getOrElse newPosition

      publishEvent(PlayerMovedToPosition(newPosition))

      actionField foreach {
        case Snake(_, to) => publishEvent(PlayerMovedBySnake(finalPosition))
        case Ladder(_, to) => publishEvent(PlayerMovedByLadder(finalPosition))
      }

      publishEventAndStay(CurrentPlayerChanged(playOrder(next).player))
        .using(data.copy(positions = positions.updated(player, finalPosition), currentPlayer = next))
  }

  when(GameOver) {
    case _ => stay
  }

  initialize()

  def publishEventAndStay(event: GameEvent) = {
    publishEvent(event)
    stay
  }

  def publishEventAndGoto(event: GameEvent, state: GameState.State) = {
    publishEvent(event)
    goto(state)
  }

  def publishEvent(event: GameEvent) =
    context.system.eventStream.publish(event)

  def nextPlayer(game: Game, currentPlayer: Int) =
    (currentPlayer + 1) % game.players.length
}

object GameStateMachine {

  case class InitializeGame(game: Game)
  case class PlayerHasRolled(player: Player, rolled: Int)
  case object GetStats

}
