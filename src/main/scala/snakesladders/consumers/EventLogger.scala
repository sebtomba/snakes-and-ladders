package snakesladders.consumers

import akka.actor.Actor
import akka.event.Logging
import snakesladders.domain.events._

class EventLogger extends Actor {

  val log = Logging(context.system, this)

  def receive = {
    case GameStarted => log.info("The game started")
    case CurrentPlayerChanged(player) => log.info("It's {} turn", player.name)
    case PlayerRolledDice(player, rolled) => log.info("Player {} rolled a {}", player.name, rolled)
    case PlayerMustRollAgain(player) => log.info("{} must roll again", player.name)
    case WrongPlayerRolled(player) => log.info("{}, it's not your turn.", player.name)
    case PlayOrderDetermined(players) => log.info("Play order is {}", players.map(p => s"${p.player.name} (${p.rolled})").mkString(", "))
    case PlayerMovedToPosition(position) => log.info("Player {} moved to field {}", position.player.name, position.position)
    case PlayerMovedBySnake(position) => log.info("Player {} moved by snake to field {}", position.player.name, position.position)
    case PlayerMovedByLadder(position) => log.info("Player {} moved by ladder to field {}", position.player.name, position.position)
    case GameIsOver(player) => log.info("The game is over and the winner is {}", player.name)
  }

}
