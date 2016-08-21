package snakesladders.domain.models

import snakesladders.domain.models.GameDefinitions.GameDefinition
import snakesladders.domain.models.Players.Player

case class Game(
  gameDefinition: GameDefinition,
  players: Seq[Player]
)
