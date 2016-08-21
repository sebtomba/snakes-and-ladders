package snakesladders.domain.services

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import snakesladders.domain.events._
import snakesladders.domain.models.GameDefinitions._
import snakesladders.domain.models.GameState._
import snakesladders.domain.models.Players._
import snakesladders.domain.models._
import snakesladders.domain.services.GameStateMachine._

class GameStateMachineSpecs
  extends TestKit(ActorSystem("GameStateMachineSpecs"))
    with ImplicitSender
    with WordSpecLike
    with Matchers {

  system.eventStream.subscribe(testActor, classOf[GameEvent])

  val player1 = Player("P1")
  val player2 = Player("P2")
  val player3 = Player("P3")
  val players = Seq(player1, player2, player3)
  val playOrder = Seq(
    PlayOrder(player2, 6),
    PlayOrder(player3, 4),
    PlayOrder(player1, 3)
  )
  val actionFields = Seq(
    Snake(34, 2),
    Ladder(3, 51)
  )
  val gameDefinition = GameDefinition(100, actionFields)
  val game = Game(gameDefinition, players)

  "GameStateMachine in DeterminingPlayOrder" when {

    "starting" should {
      "start with in the right state and data" in {
        val fsm = TestFSMRef(new GameStateMachine)
        fsm.stateName should equal(DeterminingPlayOrder)
        fsm.stateData should equal(Uninitialized)
      }
    }

    "sending InitializeGame" should {
      "send a CurrentPlayerChanged with the right player to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)

        fsm ! InitializeGame(game)
        fsm.stateName should equal(DeterminingPlayOrder)
        fsm.stateData should equal(PlayOrderData(game))

        expectMsg(GameStarted)
        expectMsg(CurrentPlayerChanged(player1))
      }
    }

    "sending PlayerHasRolled by the wrong player" should {
      "send a WrongPlayerRolled with the player to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)

        fsm ! InitializeGame(game)
        fsm ! PlayerHasRolled(player2, 6)
        fsm.stateName should equal(DeterminingPlayOrder)
        fsm.stateData should equal(PlayOrderData(game))

        expectMsg(GameStarted)
        expectMsg(CurrentPlayerChanged(player1))
        expectMsg(WrongPlayerRolled(player2))
      }
    }

    "sending PlayerHasRolled by the right player" should {
      "send a CurrentPlayerChanged with the next player to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)

        fsm ! InitializeGame(game)
        fsm ! PlayerHasRolled(player1, 3)
        fsm.stateName should equal(DeterminingPlayOrder)
        fsm.stateData should equal(PlayOrderData(game, Seq(PlayOrder(player1, 3)), 1))

        expectMsg(GameStarted)
        expectMsg(CurrentPlayerChanged(player1))
        expectMsg(CurrentPlayerChanged(player2))
      }
    }

    "sending PlayerHasRolled with a previous rolled value" should {
      "send a PlayerMustRollAgain with the current player to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)

        fsm ! InitializeGame(game)
        fsm ! PlayerHasRolled(player1, 3)
        fsm ! PlayerHasRolled(player2, 3)
        fsm.stateName should equal(DeterminingPlayOrder)
        fsm.stateData should equal(PlayOrderData(game, Seq(PlayOrder(player1, 3)), 1))

        expectMsg(GameStarted)
        expectMsg(CurrentPlayerChanged(player1))
        expectMsg(CurrentPlayerChanged(player2))
        expectMsg(PlayerMustRollAgain(player2))
      }
    }

    "sending PlayerHasRolled by all players" should {
      "go to the main game with the right play order" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap

        fsm ! InitializeGame(game)
        fsm ! PlayerHasRolled(player1, 3)
        fsm ! PlayerHasRolled(player2, 6)
        fsm ! PlayerHasRolled(player3, 4)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(GameData(game, playOrder, positions))

        expectMsg(GameStarted)
        expectMsg(CurrentPlayerChanged(player1))
        expectMsg(CurrentPlayerChanged(player2))
        expectMsg(CurrentPlayerChanged(player3))
        expectMsg(PlayOrderDetermined(playOrder))
        expectMsg(CurrentPlayerChanged(player2))
      }
    }

  }

  "GameStateMachine in MainGame" when {

    "sending PlayerHasRolled by the wrong player" should {
      "send a WrongPlayerRolled with the player to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player1, 6)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(gameData)

        expectMsg(WrongPlayerRolled(player1))
      }
    }

    "sending PlayerHasRolled by the right player" should {
      "send a PlayerMovedToPosition + CurrentPlayerChanged with the next player to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player2, 3)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(gameData.copy(
          positions = positions.updated(player2, Position(player2, 4)),
          currentPlayer = 1
        ))

        expectMsg(PlayerMovedToPosition(Position(player2, 4)))
        expectMsg(CurrentPlayerChanged(player3))
      }
    }

    "sending PlayerHasRolled and landing on the snake's tail" should {
      "not send PlayerMovedBySnake to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player2, 1)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(gameData.copy(
          positions = positions.updated(player2, Position(player2, 2)),
          currentPlayer = 1
        ))

        expectMsg(PlayerMovedToPosition(Position(player2, 2)))
        expectMsg(CurrentPlayerChanged(player3))
      }
    }

    "sending PlayerHasRolled and landing on the snake's head" should {
      "send PlayerMovedBySnake to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap.updated(player2, Position(player2, 33))
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player2, 1)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(gameData.copy(
          positions = positions.updated(player2, Position(player2, 2)),
          currentPlayer = 1
        ))

        expectMsg(PlayerMovedToPosition(Position(player2, 34)))
        expectMsg(PlayerMovedBySnake(Position(player2, 2)))
        expectMsg(CurrentPlayerChanged(player3))
      }
    }

    "sending PlayerHasRolled and landing on the ladder's top end" should {
      "not send PlayerMovedByLadder to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap.updated(player2, Position(player2, 50))
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player2, 1)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(gameData.copy(
          positions = positions.updated(player2, Position(player2, 51)),
          currentPlayer = 1
        ))

        expectMsg(PlayerMovedToPosition(Position(player2, 51)))
        expectMsg(CurrentPlayerChanged(player3))
      }
    }

    "sending PlayerHasRolled and landing on the ladder's bottom end" should {
      "send PlayerMovedByLadder to the event stream" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap.updated(player2, Position(player2, 2))
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player2, 1)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(gameData.copy(
          positions = positions.updated(player2, Position(player2, 51)),
          currentPlayer = 1
        ))

        expectMsg(PlayerMovedToPosition(Position(player2, 3)))
        expectMsg(PlayerMovedByLadder(Position(player2, 51)))
        expectMsg(CurrentPlayerChanged(player3))
      }
    }

    "sending PlayerHasRolled with a 2 when on field 99" should {
      "not send PlayerMovedToPosition to the event stream and not win" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap.updated(player2, Position(player2, 99))
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player2, 2)
        fsm.stateName should equal(MainGame)
        fsm.stateData should equal(gameData.copy(
          positions = positions.updated(player2, Position(player2, 99)),
          currentPlayer = 1
        ))

        expectMsg(CurrentPlayerChanged(player3))
      }
    }

    "sending PlayerHasRolled with a 1 when on field 99" should {
      "send PlayerMovedToPosition + GameIsOver to the event stream and go to GameOver" in {
        val fsm = TestFSMRef(new GameStateMachine)
        val positions = game.players.map(p => p -> Position(p, 1)).toMap.updated(player2, Position(player2, 99))
        val gameData = GameData(game, playOrder, positions)
        fsm.setState(MainGame, gameData)

        fsm ! PlayerHasRolled(player2, 1)
        fsm.stateName should equal(GameOver)
        fsm.stateData should equal(gameData.copy(
          positions = positions.updated(player2, Position(player2, 100)),
          currentPlayer = 1,
          winner = Some(player2)
        ))

        expectMsg(PlayerMovedToPosition(Position(player2, 100)))
        expectMsg(GameIsOver(player2))
      }
    }

  }
}
