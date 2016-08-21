package snakesladders.domain.models


object GameState {

  import snakesladders.domain.models.Players._

  sealed trait State
  case object PlayOrder extends State
  case object Game extends State
  case object GameOver extends State

  sealed trait Data
  case object Uninitialized extends Data

  case class PlayOrderData(
    game: Game,
    playOrder: Seq[PlayOrder] = Seq.empty[PlayOrder],
    playersTurn: Int = 0
  ) extends Data

  case class GameData(
    game: Game,
    playOrder: Seq[PlayOrder],
    positions: Map[Player, Position],
    playersTurn: Int = 0
  ) extends Data

}
