package snakesladders.domain.events

import snakesladders.domain.models.Players.{PlayOrder, Player, Position}

sealed trait GameEvent
case object GameStarted extends GameEvent
case class CurrentPlayerChanged(player: Player) extends GameEvent
case class PlayerMustRollAgain(player: Player) extends GameEvent
case class WrongPlayerRolled(player: Player) extends GameEvent
case class PlayOrderDetermined(playOrder: Seq[PlayOrder]) extends GameEvent
case class PlayerMovedToPosition(position: Position) extends GameEvent
case class PlayerMovedBySnake(position: Position) extends GameEvent
case class PlayerMovedByLadder(position: Position) extends GameEvent
case class GameIsOver(winner: Player) extends GameEvent
