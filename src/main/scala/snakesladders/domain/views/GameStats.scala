package snakesladders.domain.views

import snakesladders.domain.models.GameState.State
import snakesladders.domain.models.Players._

case class GameStats(
  gameId: String,
  gameState: State,
  playOrder: Seq[PlayOrder],
  positions: Map[Player, Position],
  currentPlayer: Player,
  winner: Option[Player]
)
