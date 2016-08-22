package snakesladders.domain.services

import akka.actor.{Actor, Props}
import snakesladders.domain.models.Players.Player

class Dice(nextRandom: () => Int) extends Actor {

  import snakesladders.domain.services.Dice._

  def receive = {
    case Roll(player) => sender ! DiceRolled(player, (nextRandom() % 6) + 1)
  }

}

object Dice {
  case class Roll(player: Player)
  case class DiceRolled(player: Player, rolled: Int)

  def props(nextRandom: () => Int) = Props(new Dice(nextRandom))
}
