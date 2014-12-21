package survivor

import lagcomp._

object Game {
  def nextState(state: State, inputs: Set[Event[Input]]): State = {
    val (myInputs, hisInputs) = inputs.partition(_.peer == P1)
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
  
  def nextShip(oldShip: Ship, events: Set[Event[Input]], now: Int): Ship = {
    import oldShip._
    
    val inputs = events.map(_.input)
    
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

    def inBounds(d: Double): Double = {
      if(d > maxSpeed) maxSpeed
      else if(d < -maxSpeed) -maxSpeed
      else if(d < almostZero && d > -almostZero) 0
      else d
    }
    
    Ship(
      x=x + xSpeed,
      y=y + ySpeed,
      
      xSpeed=inBounds((xOr, thrusting) match {
        case (⇦, true) => xSpeed - acceleration
        case (⇨, true) => xSpeed + acceleration
        case _ => xSpeed * decelerationRate
      }),
      
      ySpeed=inBounds((yOr, thrusting) match {
        case (⇧, true) => ySpeed - acceleration
        case (⇩, true) => ySpeed + acceleration
        case _ => ySpeed * decelerationRate
      }),
      
      xOr=if((⬄, ⇳) == orientation) xOr else orientation._1,
      yOr=if((⬄, ⇳) == orientation) yOr else orientation._2,
      
      pressed=inputs.foldLeft(pressed) { (s: Set[Key], i: Input) =>
        i.action match {
          case Press => s + i.key
          case Release => s - i.key
        }
      },
      
      dying=dying,
      dyingSince=dyingSince,
      firingSince=if(inputs.exists(k => k.key == Space && k.action == Press)) now else firingSince
    )
  }
  
  val initialState: State = State(0, Ship(32, 32), Ship(64, 64), List())
}
