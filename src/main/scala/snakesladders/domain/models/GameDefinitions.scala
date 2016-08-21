package snakesladders.domain.models

object GameDefinitions {

  case class GameDefinition(
    fieldCount: Int,
    actionFields: Seq[ActionField]
  ) {
    assert(actionFields.forall {
      case Snake(from, _) => from < fieldCount
      case Ladder(_, upTo) => upTo < fieldCount
    }, "All snakes and ladders must be within fields bound!")
  }

  sealed trait ActionField {
    val from: Int
    val to: Int
  }

  case class Snake(
    from: Int,
    to: Int
  ) extends ActionField {
    assert(from > to, "Snakes go down, not up!")
  }

  case class Ladder(
    from: Int,
    to: Int
  ) extends ActionField {
    assert(from < to, "Ladders go up, not down!")
  }

}
