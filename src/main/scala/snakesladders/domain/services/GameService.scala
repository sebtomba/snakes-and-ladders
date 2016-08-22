package snakesladders.domain.services

import akka.actor.{Actor, ActorRef, Props}
import snakesladders.domain.models.Game
import snakesladders.domain.models.GameDefinitions.{GameDefinition, Ladder, Snake}
import snakesladders.domain.models.Players.Player
import snakesladders.domain.services.Dice.{DiceRolled, Roll}
import snakesladders.domain.services.GameService._
import snakesladders.domain.services.GameStateMachine._

class GameService(game: Game, dice: ActorRef, state: ActorRef) extends Actor {

  def receive = {
    case RollDiceForPlayer(player) => dice ! Roll(player)
    case RollDiceForCurrentPlayer => state ! GetCurrentPlayer
    case DiceRolled(player, rolled) => state ! PlayerRolled(player, rolled)
    case CurrentPlayer(player) => dice ! Roll(player)
  }
}

object GameService {

  case class RollDiceForPlayer(player: Player)
  case object RollDiceForCurrentPlayer

  def props(game: Game, dice: ActorRef, state: ActorRef) = Props(new GameService(game, dice, state))

  val gameDefinition = GameDefinition(100, Seq(
    Snake(17, 13),
    Snake(52, 29),
    Snake(47, 40),
    Snake(62, 22),
    Snake(88, 18),
    Snake(95, 51),
    Snake(97, 79),
    Ladder(3, 21),
    Ladder(8, 30),
    Ladder(28, 84),
    Ladder(58, 77),
    Ladder(75, 86),
    Ladder(80, 99),
    Ladder(90, 91)
  ))

}
