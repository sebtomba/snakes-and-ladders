package snakesladders.domain.models

object Players {

  case class Player(
    name: String,
    computer: Boolean = false
  )

  case class Position(
    player: Player,
    position: Int
  )

  case class PlayOrder(
    player: Player,
    rolled: Int
  )

  implicit val playOrderOrdering = new Ordering[PlayOrder] {
    def compare(x: PlayOrder, y: PlayOrder): Int =
      y.rolled.compare(x.rolled)
  }

}
