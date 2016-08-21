package snakesladders.domain.models

import snakesladders.domain.Entity
import snakesladders.domain.models.GameDefinitions.GameDefinition
import snakesladders.domain.models.Players.Player

case class Game(
  id: String,
  gameDefinition: GameDefinition,
  players: Seq[Player]
) extends Entity
