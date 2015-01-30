package survivor

import lagcomp._

object Game {
  def nextState(state: State, inputs: Set[Action[Input]]): State = {
    val (myInputs, hisInputs) = inputs.partition(_.peer == P1)
    val t = state.time
    val aliveBlocks = state.blocks.filterNot(_.dead(t))
    val aliveTurrets = state.turrets.filterNot(_.dead(t))
    
    val inCollision: List[Shape] = Collision.of(
      List(state.ship1, state.ship2).filterNot(_.dead),
      state.shipbullets,
      aliveBlocks,
      aliveTurrets)
    
    val shipbullets: List[ShipBullet] = fires(state.ship1, t) ::: fires(state.ship2, t) :::
      state.shipbullets.diff(inCollision).filter { gf => World.contains(x=gf.x, y=gf.y) }
    
    val nextBlocks: List[Block] = aliveBlocks.map { block =>
      if(inCollision.contains(block) && !block.dying) {
        (if(!block.damaged) block.copy(damaged=true) else block.copy(dying=true)).copy(lastHit=t)
      } else block
    }
    
    val nextTurrets: List[Turret] = aliveTurrets.map { turret =>
      if(inCollision.contains(turret) && !turret.dying) {
        turret.copy(dying=true, dyingSince=t)
      } else turret
    }
    
    State(
      state.time + 1,
      ship1=nextShip(state.ship1, myInputs, t, inCollision.contains(state.ship1)),
      ship2=nextShip(state.ship2, hisInputs, t, inCollision.contains(state.ship2)),
      shipbullets.map(_.next),
      nextTurrets,
      nextBlocks)
  }
  
  def fires(ship: Ship, now: Int): List[ShipBullet] = {
    import ship._
    
    if(firing && (now - firingSince) % firingRate == 1) {
      val gunfire = ShipBullet(x=x, y=y, xOr, yOr)
      def moveABit(gf: ShipBullet) = gf.next.next
      List(moveABit(gunfire), moveABit(gunfire.copy(xOr=xOr.opposite, yOr=yOr.opposite)))
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
