package survivor

sealed trait Key
case object Up extends Key
case object Down extends Key
case object Right extends Key
case object Left extends Key
case object Space extends Key

sealed trait Action
case object Press extends Action
case object Release extends Action

sealed trait Player
case object P1 extends Player
case object P2 extends Player

sealed trait XOr { def opposite: XOr }
case object ⇦ extends XOr { def opposite = ⇨ }
case object ⇨ extends XOr { def opposite = ⇦ }
case object ⬄ extends XOr { def opposite = ⬄ }

sealed trait YOr { def opposite: YOr }
case object ⇧ extends YOr { def opposite = ⇩ }
case object ⇩ extends YOr { def opposite = ⇧ }
case object ⇳ extends YOr { def opposite = ⇳ }

case class Input(key: Key, action: Action, player: Player)

case class Event(input: Input, time: Int)

case class State(time: Int, myShip: Ship, hisShip: Ship, gunfires: List[Gunfire])

case class Gunfire(createdAt: Int, xInit: Double, yInit: Double, xOr: XOr, yOr: YOr) {
  def xPos(at: Int): Double = xOr match {
    case ⇦ => xInit - Gunfire.xSpeed * (at - createdAt)
    case ⇨ => xInit + Gunfire.xSpeed * (at - createdAt)
    case ⬄ => xInit
  }

  def yPos(at: Int): Double = yOr match {
    case ⇩ => yInit + Gunfire.ySpeed * (at - createdAt)
    case ⇧ => yInit - Gunfire.ySpeed * (at - createdAt)
    case ⇳ => yInit
  }
}

object Gunfire {
  val xSpeed = 8.0
  val ySpeed = 8.0
}

case class Ship(
  xPos: Double,
  yPos: Double,
  xSpeed: Double = 0,
  ySpeed: Double = 0,
  xOr: XOr = ⬄,
  yOr: YOr = ⇧,
  pressed: Set[Key] = Set(),
  dying: Boolean = false,
  dyingSince: Int = 0,
  firingSince: Int = 0
) {
  def thrusting: Boolean = pressed.filterNot(_ == Space).nonEmpty
  def firing: Boolean = pressed contains Space
}

object Ship {
  val size = 32
  val acceleration = 0.2
  val decelerationRate = 0.8
  val almostZero = 0.01
  val maxSpeed = 4.0
  val firingRate = 10
}

object World {
  val width = 2337
  val height = 1569
}
