package snakesladders.domain.services

import akka.actor.{Actor, ActorRef, Props}
import snakesladders.domain.models.Players.Player
import snakesladders.domain.services.Dice.{DiceRolled, Roll}
import snakesladders.domain.services.GameService.RollDiceForPlayer
import snakesladders.domain.services.GameStateMachine.{InitializeGame, PlayerRolled}

class GameService(dice: ActorRef, state: ActorRef) extends Actor {

  state ! InitializeGame

  def receive = {
    case RollDiceForPlayer(player) => dice ! Roll(player)
    case DiceRolled(player, rolled) => state ! PlayerRolled(player, rolled)
  }
}

object GameService {

  case class RollDiceForPlayer(player: Player)

  def props(dice: ActorRef, state: ActorRef) = Props(new GameService(dice, state))
}
