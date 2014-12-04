package survivor

object Game {
  def nextState(state: State, inputs: List[Input]): State = {
    val (myInputs, hisInputs) = inputs.partition(_.player == P1)
    val t = state.time
    
    val inCollision: List[Shape] = Collision.of(state.myShip :: state.hisShip :: state.gunfires, t)
    
    inCollision foreach println // TODO debug
    if(inCollision.nonEmpty) println("---------------------")
    
    val gunfires: List[Gunfire] = fires(state.myShip, t) ::: fires(state.hisShip, t) :::
      state.gunfires.filterNot { gf => World.contains(x=gf.x(t), y=gf.y(t)) }
    
    State(
      state.time + 1,
      myShip=nextShip(state.myShip, myInputs, t),
      hisShip=nextShip(state.hisShip, hisInputs, t),
      gunfires)
  }
  
  def fires(ship: Ship, now: Int): List[Gunfire] = {
    import ship._
    if(firing && (now - firingSince) % firingRate == 1) {
      val gunfire = Gunfire(now, xInit=x, yInit=y, xOr, yOr)
      List(gunfire, gunfire.copy(xOr=xOr.opposite, yOr=yOr.opposite))
    } else Nil
  }
  
  def nextShip(s: Ship, inputs: List[Input], now: Int): Ship = {
    val pressed = inputs.foldLeft(s.pressed) { (s: Set[Key], i: Input) =>
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
    val (xOr, yOr) = if((⬄, ⇳) == orientation) (s.xOr, s.yOr) else orientation

    def inBounds(d: Double): Double = {
      if(d > s.maxSpeed) s.maxSpeed
      else if(d < -s.maxSpeed) -s.maxSpeed
      else if(d < s.almostZero && d > -s.almostZero) 0
      else d
    }
    
    val xSpeed = inBounds((s.xOr, s.thrusting) match {
      case (⇦, true) => s.xSpeed - s.acceleration
      case (⇨, true) => s.xSpeed + s.acceleration
      case _ => s.xSpeed * s.decelerationRate
    })

    val ySpeed = inBounds((s.yOr, s.thrusting) match {
      case (⇧, true) => s.ySpeed - s.acceleration
      case (⇩, true) => s.ySpeed + s.acceleration
      case _ => s.ySpeed * s.decelerationRate
    })
    
    val x = s.x + s.xSpeed
    val y = s.y + s.ySpeed
    
    Ship(
      x=x,
      y=y,
      xSpeed=xSpeed,
      ySpeed=ySpeed,
      xOr,
      yOr,
      pressed,
      s.dying,
      dyingSince=s.dyingSince,
      firingSince=
        if(inputs.exists(k => k.key == Space && k.action == Press)) now else s.firingSince
    )
  }
  
  val initialState: State = State(0, Ship(32, 32), Ship(64, 64), List())
}
