package snakesladders.domain.models


object GameState {

  import snakesladders.domain.models.Players._

  sealed trait State
  case object DeterminingPlayOrder extends State
  case object MainGame extends State
  case object GameOver extends State

  sealed trait Data
  case object Uninitialized extends Data

  case class PlayOrderData(
    game: Game,
    playOrder: Seq[PlayOrder] = Seq.empty[PlayOrder],
    currentPlayer: Int = 0
  ) extends Data

  case class GameData(
    game: Game,
    playOrder: Seq[PlayOrder],
    positions: Map[Player, Position],
    currentPlayer: Int = 0,
    winner: Option[Player] = None
  ) extends Data

}
