package snakesladders.contollers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import snakesladders.controllers.GameController
import snakesladders.domain.services.GameService.RollDiceForCurrentPlayer

class GameControllerSpecs
  extends TestKit(ActorSystem("GameControllerSpecs"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "GameController" when {
    "told to roll the dice for a player" should {
      "roll the the dice" in {
        val controller = system.actorOf(GameController.props(testActor))

        controller ! "roll"
        expectMsg(RollDiceForCurrentPlayer)
      }
    }
  }

}
