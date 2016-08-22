package snakesladders.consumers

import akka.actor.{Actor, ActorRef, Props}
import snakesladders.domain.events.CurrentPlayerChanged
import snakesladders.domain.services.GameService.RollDiceForPlayer

class ComputerPlayer(gameService: ActorRef) extends Actor {

  def receive = {
    case CurrentPlayerChanged(player) if player.computer =>
      gameService ! RollDiceForPlayer(player)
  }

}

object ComputerPlayer {

  def props(gameService: ActorRef) = Props(new ComputerPlayer(gameService))

}
