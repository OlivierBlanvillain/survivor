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

case class Gunfire(createdAt: Int, xInit: Double, yInit: Double, xOr: XOr, yOr: YOr)
    extends Circle {
  
  def x(time: Int): Double = xOr match {
    case ⇦ => xInit - xSpeed * (time - createdAt)
    case ⇨ => xInit + xSpeed * (time - createdAt)
    case ⬄ => xInit
  }

  def y(time: Int): Double = yOr match {
    case ⇩ => yInit + ySpeed * (time - createdAt)
    case ⇧ => yInit - ySpeed * (time - createdAt)
    case ⇳ => yInit
  }
  
  def radius = 6.0
  def xSpeed = 8.0
  def ySpeed = 8.0
}

case class Ship(
  x: Double,
  y: Double,
  xSpeed: Double = 0,
  ySpeed: Double = 0,
  xOr: XOr = ⬄,
  yOr: YOr = ⇧,
  pressed: Set[Key] = Set(),
  dying: Boolean = false,
  dyingSince: Int = 0,
  firingSince: Int = 0
) extends Circle {
  def thrusting: Boolean = pressed.filterNot(_ == Space).nonEmpty
  def firing: Boolean = pressed contains Space
  def radius = 12.0
  def acceleration = 0.2
  def decelerationRate = 0.8
  def almostZero = 0.01
  def maxSpeed = 4.0
  def firingRate = 10
  def x(time: Int): Double = x
  def y(time: Int): Double = y
}

object World {
  val width = 75
  val height = 50
  val unitPx = 32
  val widthPx = width * unitPx
  val heightPx = height * unitPx
  
  def contains(x: Double, y: Double): Boolean = {
    x < 0 || x > World.widthPx || y < 0 || y > World.heightPx
  }
}
