package survivor

object Game {
  def nextState(state: State, inputs: List[Input]): State = {
    val (myInputs, hisInputs) = inputs.partition(_.player == Me)
    
    State(
      state.time + 1,
      myShip=nextShip(state.myShip, myInputs),
      hisShip=nextShip(state.hisShip, hisInputs),
      state.gunfires)
  }
  
  def nextShip(old: Ship, inputs: List[Input]): Ship = {
    val pressed = inputs.foldLeft(old.pressed) { (s: Set[Key], i: Input) =>
      i.action match {
        case Press => s + i.key
        case Release => s - i.key
      }
    }
    
    val xOrientation = (pressed(Left), pressed(Right)) match {
      case (true, false) => ⇦
      case (false, true) => ⇨
      case _ if(pressed(Up) || pressed(Down)) => ⬄
      case _ => old.xOrientation
    }

    val yOrientation = (pressed(Up), pressed(Down)) match {
      case (true, false) => ⇧
      case (false, true) => ⇩
      case _ if(pressed(Left) || pressed(Right)) => ⇳
      case _ => old.yOrientation
    }
    
    import Ship._
    
    def inBounds(d: Double) =
      if(d > maxSpeed) maxSpeed
      else if(d < -maxSpeed) -maxSpeed
      else if(d < almostZero && d > -almostZero) 0
      else d
    
    val xSpeed = inBounds((xOrientation, old.thrusting) match {
      case (⇦, true) => old.xSpeed - acceleration
      case (⇨, true) => old.xSpeed + acceleration
      case _ => old.xSpeed * decelerationRate
    })

    val ySpeed = inBounds((yOrientation, old.thrusting) match {
      case (⇧, true) => old.ySpeed - acceleration
      case (⇩, true) => old.ySpeed + acceleration
      case _ => old.ySpeed * decelerationRate
    })
    
    val xPosition = old.xPosition + xSpeed
    val yPosition = old.yPosition + ySpeed
    
    Ship(
      xPosition=xPosition,
      yPosition=yPosition,
      xSpeed=xSpeed,
      ySpeed=ySpeed,
      xOrientation,
      yOrientation,
      pressed,
      old.dying)
  }
  
  val initialState: State = State(0, Ship(32, 32), Ship(64, 64), List())
}

case class State(time: Int, myShip: Ship, hisShip: Ship, gunfires: List[Gunfire])

case class Gunfire()

case class Ship(
  xPosition: Double,
  yPosition : Double,
  xSpeed: Double = 0,
  ySpeed: Double = 0,
  xOrientation: XOr = ⬄,
  yOrientation: YOr = ⇧,
  pressed: Set[Key] = Set(),
  dying: Boolean = false
) {
  def thrusting: Boolean = !pressed.isEmpty
  def firing: Boolean = pressed contains Space
}

object Ship {
  val size = 32
  val acceleration = 0.2
  val decelerationRate = 0.8
  val almostZero = 0.01
  val maxSpeed = 4
}
