package snakesladders.domain.services

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import snakesladders.domain.models.Game
import snakesladders.domain.models.Players.Player
import snakesladders.domain.services.Dice.{DiceRolled, Roll}
import snakesladders.domain.services.GameService.{RollDiceForCurrentPlayer, RollDiceForPlayer}
import snakesladders.domain.services.GameStateMachine.{CurrentPlayer, GetCurrentPlayer, PlayerRolled}

class GameServiceSpecs
  extends TestKit(ActorSystem("GameServiceSpecs"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val player = Player("p1")
  val game = Game(GameService.gameDefinition, Seq(player))

  "GameService" when {

    "sending RollDiceForPlayer" should {
      "roll the dice" in {
        val service = system.actorOf(GameService.props(game, testActor, testActor))
        service ! RollDiceForPlayer(player)
        expectMsg(Roll(player))
      }
    }

    "sending RollDiceForCurrentPlayer" should {
      "ask state for the current player" in {
        val service = system.actorOf(GameService.props(game, testActor, testActor))
        service ! RollDiceForCurrentPlayer
        expectMsg(GetCurrentPlayer)
      }
    }

    "sending CurrentPlayer" should {
      "roll the dice for this player" in {
        val service = system.actorOf(GameService.props(game, testActor, testActor))
        service ! CurrentPlayer(player)
        expectMsg(Roll(player))
      }
    }

    "sending DiceRolled" should {
      "send PlayerRolled to game state" in {
        val service = system.actorOf(GameService.props(game, testActor, testActor))
        service ! DiceRolled(player, 6)
        expectMsg(PlayerRolled(player, 6))
      }
    }
  }

}
