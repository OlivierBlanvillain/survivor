package survivor

object Game {
  def nextState(state: State, inputs: List[Input]): State = {
    val ship = inputs.foldLeft(state.ship) { (ship: Ship, input: Input) =>
      (input.key, input.action) match {
        case (Up, Press) => ship.copy(yor = ⬆)
        case (Up, Release) => ship.copy(yor = ⬍)
        case (Down, Press) => ship.copy(yor = ⬇)
        case (Down, Release) => ship.copy(yor = ⬍)
        case (Right, Press) => ship.copy(xor = ➡)
        case (Right, Release) => ship.copy(xor = ⬌)
        case (Left, Press) => ship.copy(xor = ⬅)
        case (Left, Release) => ship.copy(xor = ⬌)
        case (Space, Press) => ship.copy(firing = true)
        case (Space, Release) => ship.copy(firing = false)
      }
    }
    if(!inputs.isEmpty) {
      System.err.print("In " + inputs)
      System.err.print("Pre " + state.ship)
      System.err.print("Post " + ship)
    }
    
    state.copy(time = state.time + 1, ship = ship)
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
  xor: XOr = ⬌,
  yor: YOr = ⬆,
  thrusting: Boolean = false,
  dying: Boolean = false,
  firing: Boolean = false
) {
  val accelMultiplier = 0.015
  val almostZero = 0.02
  val decelRate = 0.8
  val size = 32
  val dMax = 0.25
}
