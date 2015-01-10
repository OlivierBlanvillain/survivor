package survivor

sealed trait Key
case object Up extends Key
case object Down extends Key
case object Right extends Key
case object Left extends Key
case object Space extends Key

sealed trait KeyAction
case object Press extends KeyAction
case object Release extends KeyAction

sealed trait XOr { def opposite: XOr }
case object ⇦ extends XOr { def opposite = ⇨ }
case object ⇨ extends XOr { def opposite = ⇦ }
case object ⬄ extends XOr { def opposite = ⬄ }

sealed trait YOr { def opposite: YOr }
case object ⇧ extends YOr { def opposite = ⇩ }
case object ⇩ extends YOr { def opposite = ⇧ }
case object ⇳ extends YOr { def opposite = ⇳ }

case class Input(key: Key, action: KeyAction)

case class State(
  time: Int,
  myShip: Ship,
  hisShip: Ship,
  gunfires: List[Gunfire],
  blocks: List[Block])

case class Block(
  x: Double,
  y: Double,
  deadSince: Int = Int.MinValue,
  damaged: Boolean=false)

case class Gunfire(
  x: Double,
  y: Double,
  xOr: XOr,
  yOr: YOr)
extends Circle {
  val xSpeed = xOr match {
    case ⇦ => 8
    case ⇨ => -8
    case ⬄ => 0
  }
  val ySpeed = yOr match {
    case ⇩ => -8
    case ⇧ => 8
    case ⇳ => 0
  }
  val radius = 3.0
  def next = Gunfire(x + xSpeed, y + ySpeed, xOr, yOr)
}

case class Ship(
  x: Double,
  y: Double,
  xRespawn: Double,
  yRespawn: Double,
  xSpeed: Double = 0,
  ySpeed: Double = 0,
  xOr: XOr = ⬄,
  yOr: YOr = ⇧,
  pressed: Set[Key] = Set(),
  dead: Boolean = false,
  deadSince: Int = 0,
  firingSince: Int = 0
) extends Circle {
  def thrusting: Boolean = pressed.filterNot(_ == Space).nonEmpty
  def firing: Boolean = !dead && pressed.contains(Space)
  def x(time: Int): Double = x
  def y(time: Int): Double = y
  val radius = 14.0
  val acceleration = 0.2
  val decelerationRate = 0.8
  val almostZero = 0.01
  val maxSpeed = 4.0
  val firingRate = 10
  val respawnDelay = 100
}

object World {
  val width = 35
  val height = 22
  val unitPx = 32
  val widthPx = width * unitPx
  val heightPx = height * unitPx
  
  def contains(x: Double, y: Double): Boolean =
    !(x < 0 || x > World.widthPx || y < 0 || y > World.heightPx)
}
