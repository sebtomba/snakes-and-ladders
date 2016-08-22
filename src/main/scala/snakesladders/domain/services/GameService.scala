package snakesladders.domain.services

import akka.actor.{Actor, ActorRef, Props}
import snakesladders.domain.models.Players.Player
import snakesladders.domain.services.Dice.{DiceRolled, Roll}
import snakesladders.domain.services.GameService._
import snakesladders.domain.services.GameStateMachine._

class GameService(dice: ActorRef, state: ActorRef) extends Actor {

  state ! InitializeGame

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

  def props(dice: ActorRef, state: ActorRef) = Props(new GameService(dice, state))
}
