package survivor

object Game {
  def nextState(state: State, inputs: List[Input]): State = {
    state.copy(time = state.time + 1)
  }
  
  val initialState: State = State(0, Ship(32, 32), List())
}

case class State(time: Int, ship: Ship, gunfires: List[Gunfire])

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
