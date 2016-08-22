package snakesladders.consumers

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import snakesladders.domain.events.CurrentPlayerChanged
import snakesladders.domain.models.Players.Player
import snakesladders.domain.services.GameService.RollDiceForPlayer

class ComputerPlayerSpecs
  extends TestKit(ActorSystem("ComputerPlayerSpecs"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ComputerPlayer" when {

    "received the CurrentPlayerChanged event for computer player" should {
      "send RollDiceForPlayer to the game service" in {
        val computer = system.actorOf(ComputerPlayer.props(testActor))
        val player = Player("p1", computer = true)

        system.eventStream.subscribe(computer, classOf[CurrentPlayerChanged])
        system.eventStream.publish(CurrentPlayerChanged(player))
        expectMsg(RollDiceForPlayer(player))
      }
    }

    "received the CurrentPlayerChanged event for human player" should {
      "not send RollDiceForPlayer to the game service" in {
        val computer = system.actorOf(ComputerPlayer.props(testActor))
        val player = Player("p1")

        system.eventStream.subscribe(computer, classOf[CurrentPlayerChanged])
        system.eventStream.publish(CurrentPlayerChanged(player))
        expectNoMsg(1 second)
      }
    }
  }

}
