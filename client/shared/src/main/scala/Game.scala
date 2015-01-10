package survivor

import lagcomp._

object Game {
  def nextState(state: State, inputs: Set[Action[Input]]): State = {
    val (myInputs, hisInputs) = inputs.partition(_.peer == P1)
    val t = state.time
    
    val inCollision: List[Shape] = Collision.of(
      List(state.myShip, state.hisShip).filterNot(_.dead) ::: state.gunfires, t)
    
    val gunfires: List[Gunfire] = fires(state.myShip, t) ::: fires(state.hisShip, t) :::
      state.gunfires.diff(inCollision).filter { gf => World.contains(x=gf.x(t), y=gf.y(t)) }
    
    State(
      state.time + 1,
      myShip=nextShip(state.myShip, myInputs, t, inCollision.contains(state.myShip)),
      hisShip=nextShip(state.hisShip, hisInputs, t, inCollision.contains(state.hisShip)),
      gunfires,
      state.blocks)
  }
  
  def fires(ship: Ship, now: Int): List[Gunfire] = {
    import ship._
    
    if(firing && (now - firingSince) % firingRate == 1) {
      val gunfire = Gunfire(now, xInit=x, yInit=y, xOr, yOr)
      List(gunfire, gunfire.copy(xOr=xOr.opposite, yOr=yOr.opposite))
    } else Nil
  }
  
  def nextShip(oldShip: Ship, events: Set[Action[Input]], now: Int, collision: Boolean): Ship = {
    import oldShip._
    
    val inputs = events.map(_.input).toList.sortWith {
      case (Input(_, Press), Input(_, Release)) => true
      case _ => false
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

    def inBounds(d: Double): Double = {
      if(d > maxSpeed) maxSpeed
      else if(d < -maxSpeed) -maxSpeed
      else if(d < almostZero && d > -almostZero) 0
      else d
    }
    
    val newPressed = inputs.foldLeft(pressed) { (s: Set[Key], i: Input) =>
      i.action match {
        case Press => s + i.key
        case Release => s - i.key
      }
    }
    
    if(dead && (now - deadSince < respawnDelay)) {
      oldShip.copy(pressed=newPressed)
    } else if(dead) {
      Ship(
        x=xRespawn,
        y=yRespawn,
        xRespawn=xRespawn,
        yRespawn=yRespawn,
        pressed=newPressed,
        dead=false,
        deadSince=deadSince,
        firingSince=firingSince
      )
    } else {
      Ship(
        x=if(World.contains(x + xSpeed, y)) x + xSpeed else x,
        y=if(World.contains(x, y + ySpeed)) y + ySpeed else y,
        xRespawn=xRespawn,
        yRespawn=yRespawn,
        
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
        pressed=newPressed,
        dead=collision,
        deadSince=if(collision) now else deadSince,
        firingSince=if(inputs.exists(k => k.key == Space && k.action == Press)) now else firingSince
      )
    }
  }
}
