package snakesladders.consumers

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import snakesladders.domain.events.{CurrentPlayerChanged, GameEvent, PlayerMustRollAgain}
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

  val humanPlayer = Player("p1")
  val computerPlayer = Player("p2", computer = true)
  val computer = system.actorOf(ComputerPlayer.props(testActor))
  system.eventStream.subscribe(computer, classOf[GameEvent])

  "ComputerPlayer" when {

    "received the CurrentPlayerChanged event for computer player" should {
      "send RollDiceForPlayer to the game service" in {

        system.eventStream.publish(CurrentPlayerChanged(computerPlayer))
        expectMsg(RollDiceForPlayer(computerPlayer))
      }
    }

    "received the CurrentPlayerChanged event for human player" should {
      "not send RollDiceForPlayer to the game service" in {
        val computer = system.actorOf(ComputerPlayer.props(testActor))

        system.eventStream.publish(CurrentPlayerChanged(humanPlayer))
        expectNoMsg(1 second)
      }
    }

    "received the PlayerMustRollAgain event for computer player" should {
      "send RollDiceForPlayer to the game service" in {
        val computer = system.actorOf(ComputerPlayer.props(testActor))

        system.eventStream.publish(PlayerMustRollAgain(computerPlayer))
        expectMsg(RollDiceForPlayer(computerPlayer))
      }
    }

    "received the PlayerMustRollAgain event for human player" should {
      "not send RollDiceForPlayer to the game service" in {
        val computer = system.actorOf(ComputerPlayer.props(testActor))

        system.eventStream.publish(PlayerMustRollAgain(humanPlayer))
        expectNoMsg(1 second)
      }
    }
  }

}
