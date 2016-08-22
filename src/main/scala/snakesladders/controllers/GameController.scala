package snakesladders.controllers

import akka.actor.{Actor, ActorRef, Props}
import snakesladders.domain.services.GameService.RollDiceForCurrentPlayer

class GameController(gameService: ActorRef) extends Actor {

  def receive = {
    case "roll" => gameService ! RollDiceForCurrentPlayer
  }

}

object GameController {

  def props(gameService: ActorRef) = Props(new GameController(gameService))

}
