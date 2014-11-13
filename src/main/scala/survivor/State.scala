package survivor

sealed trait Key
case object Up extends Key
case object Down extends Key
case object Right extends Key
case object Left extends Key
case object Shift extends Key

sealed trait Player
case object Me extends Player
case object He extends Player

case class Event(key: Key, pressed: Boolean, player: Player, time: Double)
case class State(ship: Ship, gunfires: List[Gunfire])

case class Gunfire()

case class Ship(
  x: Int,
  y: Int,
  dx: Int = 0,
  dy: Int = 0,
  thrustingx: Int = 0,
  thrustingy: Int = 0,
  dying: Boolean = false,
  firing: Boolean = false
) {
  val accelMultiplier = 0.015
  val almostZero = 0.02
  val decelRate = 0.8
  val size = 32
  val dMax = 0.25
}
