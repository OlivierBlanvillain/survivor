package survivor

object Game {
  def nextState(state: State, inputs: List[Input]): State = {
    val pressed = inputs.foldLeft(state.ship.pressed) { (s: Set[Key], i: Input) =>
      i.action match {
        case Press => s + i.key
        case Release => s - i.key
      }
    }
    
    val xOrientation = (pressed(Left), pressed(Right)) match {
      case (true, false) => ⇦
      case (false, true) => ⇨
      case _ if(pressed(Up) || pressed(Down)) => ⬄
      case _ => state.ship.xOrientation
    }

    val yOrientation = (pressed(Up), pressed(Down)) match {
      case (true, false) => ⇧
      case (false, true) => ⇩
      case _ if(pressed(Left) || pressed(Right)) => ⇳
      case _ => state.ship.yOrientation
    }
    
    val xSpeed = Math.min(Ship.maxSpeed, state.ship.xSpeed * Ship.accelMultiplier)
    val ySpeed = Math.min(Ship.maxSpeed, state.ship.ySpeed * Ship.accelMultiplier)
    
    val ship = Ship(
      state.ship.xPosition,
      state.ship.yPosition,
      state.ship.xSpeed,
      state.ship.ySpeed,
      xOrientation,
      yOrientation,
      pressed,
      state.ship.dying)
    
    State(
      state.time + 1,
      ship,
      state.gunfires)
  }
  
  val initialState: State = State(0, Ship(32, 32), List())
}

case class State(time: Int, ship: Ship, gunfires: List[Gunfire])

case class Gunfire()

case class Ship(
  xPosition: Int,
  yPosition : Int,
  xSpeed: Int = 0,
  ySpeed: Int = 0,
  xOrientation: XOr = ⬄,
  yOrientation: YOr = ⇧,
  pressed: Set[Key] = Set(),
  dying: Boolean = false
) {
  def thrusting: Boolean = !pressed.isEmpty
  def firing: Boolean = pressed contains Space
}

object Ship {
  val accelMultiplier = 0.015
  val almostZero = 0.02
  val decelRate = 0.8
  val size = 32
  val maxSpeed = 0.25
}
