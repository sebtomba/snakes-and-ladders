package snakesladders.domain.services

import akka.actor.{Actor, Props}
import snakesladders.domain.events.PlayerRolledDice
import snakesladders.domain.models.Players.Player

class Dice(nextRandom: () => Int) extends Actor {

  import snakesladders.domain.services.Dice._

  def receive = {
    case Roll(player) =>
      val rolled = (nextRandom() % 6) + 1
      context.system.eventStream.publish(PlayerRolledDice(player, rolled))
      sender ! DiceRolled(player, rolled)
  }

}

object Dice {
  case class Roll(player: Player)
  case class DiceRolled(player: Player, rolled: Int)

  def props(nextRandom: () => Int) = Props(new Dice(nextRandom))
}
