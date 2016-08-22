package snakesladders.domain.services

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import snakesladders.domain.models.Players.Player
import snakesladders.domain.services.Dice.{DiceRolled, Roll}

class DiceSpecs
  extends TestKit(ActorSystem("DiceSpecs"))
    with ImplicitSender
    with WordSpecLike
    with Matchers {

  "Dice" when {
    "told to roll the dice for a player" should {
      "roll the the dice" in {
        val dice = system.actorOf(Dice.props(() => 10), "dice")
        val player = Player("p1")
        dice ! Roll(player)
        expectMsg(DiceRolled(player, 5))
      }
    }
  }

}
