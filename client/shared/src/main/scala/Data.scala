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

sealed trait Or
sealed trait XOr { def opposite: XOr }
case object ⇦ extends XOr with Or { def opposite = ⇨ }
case object ⇨ extends XOr with Or { def opposite = ⇦ }
case object ⬄ extends XOr { def opposite = ⬄ }

sealed trait YOr { def opposite: YOr }
case object ⇧ extends YOr with Or { def opposite = ⇩ }
case object ⇩ extends YOr with Or { def opposite = ⇧ }
case object ⇳ extends YOr { def opposite = ⇳ }

case class Input(key: Key, action: KeyAction)

case class State(
  time: Int,
  ship1: Ship,
  ship2: Ship,
  shipbullets: List[ShipBullet],
  turrets: List[Turret],
  blocks: List[Block])

case class Block(
  row: Int,
  col: Int,
  damaged: Boolean = false,
  dying: Boolean = false,
  lastHit: Int = 0
) extends SingleCellRectangle {
  val x = World.unitPx * col.toDouble + World.unitPx/2
  val y = World.unitPx * row.toDouble + World.unitPx/2
  val halfWeight = 12.0
  val halfHeight = 12.0
  def exploding(time: Int) = damaged && time - lastHit < 16
  def dead(time: Int) = dying && !exploding(time)
}

case class ShipBullet(
  x: Double,
  y: Double,
  xOr: XOr,
  yOr: YOr
) extends Circle {
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
  def next = ShipBullet(x + xSpeed, y + ySpeed, xOr, yOr)
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
  val decelerationRate = 0.9
  val almostZero = 0.01
  val maxSpeed = 4.0
  val firingRate = 10
  val respawnDelay = 100
}

case class Turret(
  row: Int,
  col: Int,
  orientation: Or,
  range: Double = 0,
  dying: Boolean = false,
  dyingSince: Int = 0
) extends SingleCellRectangle {
  val x = World.unitPx * col.toDouble + World.unitPx/2
  val y = World.unitPx * row.toDouble + World.unitPx/2
  val halfWeight = 16.0
  val halfHeight = 16.0
  def dead(time: Int) = dying && time - dyingSince < 16
}

object World {
  val width = Initial.map.lines.next.size
  val height = Initial.map.lines.size
  val unitPx = 32
  val widthPx = width * unitPx
  val heightPx = height * unitPx
  def contains(x: Double, y: Double): Boolean =
    !(x < 0 || x > World.widthPx || y < 0 || y > World.heightPx)
}
