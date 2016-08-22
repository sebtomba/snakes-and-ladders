package snakesladders

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn._

import akka.actor.{ActorSystem, Props}
import snakesladders.consumers.{ComputerPlayer, EventLogger}
import snakesladders.controllers.GameController
import snakesladders.domain.events.GameEvent
import snakesladders.domain.models.Game
import snakesladders.domain.models.Players.Player
import snakesladders.domain.services.GameStateMachine.InitializeGame
import snakesladders.domain.services.{Dice, GameService, GameStateMachine}

object SnakesAndLadders extends App {

  val playerCount = getIntFromUser("How many players? (2 - 4): ", 2, 4)
  val computerPlayerCount = getIntFromUser(s"How many computer players? (0 - $playerCount): ", 0, playerCount)
  val humanPlayerCount = playerCount - computerPlayerCount

  val computerPlayer: Seq[Player] =
    if(computerPlayerCount > 0)
      1 to computerPlayerCount map (c => Player(s"Computer Player $c", computer = true))
    else Seq.empty[Player]

  val humanPlayer: Seq[Player] =
    if(humanPlayerCount > 0)
      1 to humanPlayerCount map (p => Player(s"Player $p"))
    else Seq.empty[Player]

  val players = humanPlayer ++ computerPlayer
  val game = Game(GameService.gameDefinition, players)
  val system = ActorSystem("SnakesAndLadders")
  val random = () => scala.util.Random.nextInt(1000)
  val state = system.actorOf(Props[GameStateMachine], "state")
  val dice = system.actorOf(Dice.props(random), "dice")
  val service = system.actorOf(GameService.props(game, dice, state))
  val controller = system.actorOf(GameController.props(service))
  val computer = system.actorOf(ComputerPlayer.props(service))
  val logger = system.actorOf(Props[EventLogger])

  system.eventStream.subscribe(computer, classOf[GameEvent])
  system.eventStream.subscribe(logger, classOf[GameEvent])

  state ! InitializeGame(game)

  while (true) {
    readLine("> ").trim.toLowerCase match {
      case "exit" => Await.ready(system.terminate().map(_ => System.exit(0)), 5.seconds)
      case "roll" => controller ! "roll"
      case _ =>
    }
  }

  @tailrec
  def getIntFromUser(message: String, min: Int, max: Int): Int = {
    print(message)
    val i = readInt()
    if (i < min || i > max) {
      println(s"The number must be greater than or equal $min and smaller than or equal $max")
      getIntFromUser(message, min, max)
    } else i

  }
}
