package snakesladders.domain.models

case object Players {

  import snakesladders.domain.Entity

  case class Player(
    id: String,
    name: String,
    computer: Boolean = false
  ) extends Entity

  case class Position(
    player: Player,
    position: Int
  )

  case class PlayOrder(
    player: Player,
    rolled: Int
  )

}
