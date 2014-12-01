package survivor

object Game {
  def nextState(state: State, inputs: List[Input]): State = {
    val (myInputs, hisInputs) = inputs.partition(_.player == P1)
    val t = state.time
    
    val gunfires: List[Gunfire] = fires(state.myShip, t) ::: fires(state.hisShip, t) :::
      state.gunfires.filterNot { gf =>
        gf.xPos(t) < 0 || gf.xPos(t) > World.width || gf.yPos(t) < 0 || gf.yPos(t) > World.height
      }
    
    State(
      state.time + 1,
      myShip=nextShip(state.myShip, myInputs, t),
      hisShip=nextShip(state.hisShip, hisInputs, t),
      gunfires)
  }
  
  def fires(ship: Ship, now: Int): List[Gunfire] = {
    import ship._, Ship._
    if(firing && (now - firingSince) % firingRate == 0) {
      val gunfire = Gunfire(now, xInit=xPos, yInit=yPos, xOr, yOr)
      List(gunfire, gunfire.copy(xOr=xOr.opposite, yOr=yOr.opposite))
    } else Nil
  }
  
  def nextShip(old: Ship, inputs: List[Input], now: Int): Ship = {
    val pressed = inputs.foldLeft(old.pressed) { (s: Set[Key], i: Input) =>
      i.action match {
        case Press => s + i.key
        case Release => s - i.key
      }
    }
    
    val orientation = (
      (pressed(Left), pressed(Right)) match {
        case (true, false) => ⇦
        case (false, true) => ⇨
        case _ => ⬄
      },
      (pressed(Up), pressed(Down)) match {
        case (true, false) => ⇧
        case (false, true) => ⇩
        case _ => ⇳
      }
    )
    val (xOr, yOr) = if((⬄, ⇳) == orientation) (old.xOr, old.yOr) else orientation

    import Ship._
    
    def inBounds(d: Double): Double = {
      if(d > maxSpeed) maxSpeed
      else if(d < -maxSpeed) -maxSpeed
      else if(d < almostZero && d > -almostZero) 0
      else d
    }
    
    val xSpeed = inBounds((xOr, old.thrusting) match {
      case (⇦, true) => old.xSpeed - acceleration
      case (⇨, true) => old.xSpeed + acceleration
      case _ => old.xSpeed * decelerationRate
    })

    val ySpeed = inBounds((yOr, old.thrusting) match {
      case (⇧, true) => old.ySpeed - acceleration
      case (⇩, true) => old.ySpeed + acceleration
      case _ => old.ySpeed * decelerationRate
    })
    
    val xPos = old.xPos + xSpeed
    val yPos = old.yPos + ySpeed
    
    Ship(
      xPos=xPos,
      yPos=yPos,
      xSpeed=xSpeed,
      ySpeed=ySpeed,
      xOr,
      yOr,
      pressed,
      old.dying,
      dyingSince=old.dyingSince,
      firingSince=
        if(inputs.exists(k => k.key == Space && k.action == Press)) now else old.firingSince
    )
  }
  
  val initialState: State = State(0, Ship(32, 32), Ship(64, 64), List())
}
