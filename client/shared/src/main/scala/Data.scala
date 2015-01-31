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
  blocks: List[Block],
  explosions: List[Explosion]=Nil)

case class Block(
  row: Int,
  col: Int,
  damaged: Boolean = false,
  dying: Boolean = false,
  lastHit: Int = 0
) extends Rectangle {
  val x = World.unitPx * col.toDouble + World.halfUnitPx
  val y = World.unitPx * row.toDouble + World.halfUnitPx
  val halfWidth = 12
  val halfHeight = 12
  def explosion(time: Int) = Explosion(x, y, cells, time)
  val cell = Point(x=col,y=row)
  val cells = List(cell)
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
  override val cells = List(Point(x.toInt / World.unitPx, y.toInt / World.unitPx))
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
  val thrusting: Boolean = pressed.filterNot(_ == Space).nonEmpty
  val firing: Boolean = !dead && pressed.contains(Space)
  val radius = 13.0
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
  range: Int = 5
) extends Rectangle {
  val x = World.unitPx * col.toDouble + World.halfUnitPx
  val y = World.unitPx * row.toDouble + World.halfUnitPx
  val halfWidth = 16
  val halfHeight = 16
  def explosion(time: Int) = Explosion(x, y, cells, time)
  def bullet(time: Int) = {
    val delta = time % (range * World.unitPx)
    orientation match {
      case ⇧ => TurretYBullet(x, y - delta)
      case ⇩ => TurretYBullet(x, y + delta)
      case ⇦ => TurretXBullet(x - delta, y)
      case ⇨ => TurretXBullet(x + delta, y)
    }
  }
  val cell = Point(x=col,y=row)
  val cells = List(cell)
}

trait TurretBullet extends MultiHitbox

case class TurretYBullet(x: Double, y: Double) extends TurretBullet { self =>
  val hitbox1 = new Hitbox(x=self.x + 7, y=self.y, halfWidth=1, halfHeight=8)
  val hitbox2 = new Hitbox(x=self.x - 7, y=self.y, halfWidth=1, halfHeight=8)
  val cells = {
    val c1 = Point((x + 8).toInt / World.unitPx, y.toInt / World.unitPx)
    val c2 = Point((x - 8).toInt / World.unitPx, y.toInt / World.unitPx)
    if(c1 == c2) List(c1) else List(c1, c2)
  }
}

case class TurretXBullet(x: Double, y: Double) extends TurretBullet { self =>
  val hitbox1 = new Hitbox(x=self.x, y=self.y + 7, halfWidth=8, halfHeight=1)
  val hitbox2 = new Hitbox(x=self.x, y=self.y - 7, halfWidth=8, halfHeight=1)
  val cells = {
    val c1 = Point(x.toInt / World.unitPx, (y + 8).toInt / World.unitPx)
    val c2 = Point(x.toInt / World.unitPx, (y - 8).toInt / World.unitPx)
    if(c1 == c2) List(c1) else List(c1, c2)
  }
}

case class Explosion(
  x: Double,
  y: Double,
  cells: List[Point],
  startedAt: Int
) extends Rectangle {
  val halfWidth = 12
  val halfHeight = 12
  def over(time: Int) = time - startedAt > 12
}
