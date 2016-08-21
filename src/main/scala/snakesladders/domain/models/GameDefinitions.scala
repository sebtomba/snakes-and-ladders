package snakesladders.domain.models

case object GameDefinitions {

  import snakesladders.domain.Entity

  case class GameDefinition(
    id: String,
    fieldCount: Int,
    actionFields: Seq[ActionField]
  ) extends Entity {
    assert(actionFields.forall {
      case Snake(from, _) => from < fieldCount
      case Ladder(_, upTo) => upTo < fieldCount
    }, "All snakes and ladders must be within fields bound!")
  }

  sealed trait ActionField

  case class Snake(
    from: Int,
    downTo: Int
  ) extends ActionField {
    assert(from > downTo, "Snakes go down, not up!")
  }

  case class Ladder(
    from: Int,
    upTo: Int
  ) extends ActionField {
    assert(from < upTo, "Ladders go up, not down!")
  }

}
