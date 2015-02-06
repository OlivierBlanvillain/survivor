
package survivor

import scala.collection.mutable
import scala.util.Random
import scalajs.js.annotation.JSExport

import upickle._
import org.scalajs.dom
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._

@JSExport("ReactUi")
object ReactUi {
  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    final def option[A](a: => A): Option[A] = if (b) Some(a) else None
  }

  def component[T <: AnyRef](r: T => ReactElement) = ReactComponentB[T]("")
    .render(r)
    .shouldComponentUpdate((scope, props, _) => scope.props ne props)
    .build

  val ship = component[(Ship, Int)] { case (s, now) =>
    import s._
    
    if(dead) {
      val deadTime = now - deadSince
      if(deadTime > 0.5*respawnDelay) {
        div()
      } else {
        val frame =
          if(deadTime % 8 == 3 || deadTime % 8 == 4) 0
          else if(deadTime > 0.4 * respawnDelay) 4
          else if(deadTime > 0.3 * respawnDelay) 3
          else if(deadTime > 0.2 * respawnDelay) 2
          else if(deadTime > 0.1 * respawnDelay) 1
          else 3
        div(
          for(i <- List(-1, 0, 1); j <- List(-1, 0, 1) if(!(i == 0 && j == 0))) yield
            div(cls:="big-explosion-"+frame, top:=y+8*deadTime*i, left:=x+8*deadTime*j)
        )()
      }
    } else {
      val clazz = List(
        Some("ship"),
        thrusting option "thrusting",
        xOr == ⇦ option "thrust-left",
        xOr == ⇨ option "thrust-right",
        yOr == ⇧ option "thrust-up",
        yOr == ⇩ option "thrust-down",
        dead option "hidden"
      ) flatMap (_.toList) mkString " "
      
      div(cls:=clazz, top:=y, left:=x)()
    }
  }
  
  val shipbullets = component[List[ShipBullet]] { sb =>
    div(sb.map { shipbullet => div(cls:="ship-gunfire", top:=shipbullet.y, left:=shipbullet.x) })
  }
  
  val turretbullets = component[List[TurretBullet]] { tb =>
    div(tb.map { turretbullet =>
      val tpe = turretbullet match {
        case t: TurretYBullet => "down"
        case t: TurretXBullet => "right"
      }
      val translate = s"translate3d(${turretbullet.x}px, ${turretbullet.y}px, 0px)"
      div(cls:=s"turret-gunfire $tpe", transform:=translate)
    })
  }
  
  def hardCodedtype(s: Shape) = {
    (s.x < 1000, s.y < 700) match {
      case (true, true) => 0
      case (true, false) => 1
      case (false, true) => 3
      case (false, false) => 2
    }
  }
  
  val blocks = component[List[Block]] { bs =>
    div(id:="blocks", bs.map { block =>
      val tpe = hardCodedtype(block)
      div(
        id:=block.cell.x + "-" + block.cell.y,
        cls:=s"block type-$tpe",
        top:=block.y, left:=block.x)
    })
  }
  
  val turrets = component[List[Turret]] { ts =>
    div(ts.map { turret =>
      val tpe = turret.orientation match {
        case ⇧ => "top"
        case ⇩ => "down"
        case ⇦ => "left"
        case ⇨ => "right"
      }
      div(cls:=s"turret type-$tpe", top:=turret.y, left:=turret.x)
    })
  }
  
  val explosions = component[List[Explosion]] { es =>
    div(es.map { explosion =>
      div(cls:="explosion", top:=explosion.y, left:=explosion.x)
    })
  }
  
  val walls = component[List[Wall]] { ws => 
    div(ws.map { wall =>
      val tpe = hardCodedtype(wall)
      val clazz = wall match {
        case w: ─ => "horizontal"
        case w: │ => "vertical"
        case w: ┌ => "upRight"
        case w: ┐ => "rightDown"
        case w: ┘ => "downLeft"
        case w: └ => "downRight"
      }
      div(cls:=s"wall type-$tpe $clazz", top:=wall.y, left:=wall.x)
    })
  }
  
  val world = component[State] { s =>
    div(
      ship((s.ship1, s.time)),
      ship((s.ship2, s.time)),
      shipbullets(s.shipbullets),
      turrets(s.turrets),
      turretbullets(s.turrets.map(_.bullet(s.time))),
      blocks(World.initialBlocks),
      explosions(s.explosions),
      walls(World.walls))
  }
  
  // val hitboxes = component[List[Shape]] { xs =>
  //   div(xs.flatMap { 
  //     case h: MultiHitbox => List(h.hitbox1, h.hitbox2)
  //     case s => List(s)
  //   } map {
  //     case c: Circle =>
  //       div(
  //         top:=c.y-c.radius,
  //         left:=c.x-c.radius,
  //         height:=c.radius*2,
  //         width:=c.radius*2,
  //         borderRadius:="50%",
  //         background:="rgba(255,0,0,0.3)",
  //         position:="absolute")
  //     case r: Rectangle =>
  //       div(
  //         top:=r.y-r.halfHeight,
  //         left:=r.x-r.halfWidth,
  //         height:=r.halfHeight*2,
  //         width:=r.halfWidth*2,
  //         background:="rgba(255,0,0,0.3)",
  //         position:="absolute")
  //     case _ => div()
  //   })
  // }
  //
  // hitboxes(List(s.ship1, s.ship2).filterNot(_.dead)),
  // hitboxes(s.shipbullets),
  // hitboxes(s.turrets),
  // hitboxes(s.turrets.map(_.bullet(s.time))),
  // hitboxes(s.blocks),
  // hitboxes(s.explosions),
  // hitboxes(World.walls),
  
  def render(me: lagcomp.Peer)(state: State): Unit = {
    UglyDomUpdates(me)(state)
    world(state) render dom.document.getElementById("world")
  }
  
  @JSExport
  def renderString(pickleMe: String, pickleState: String): Unit = {
    val state = upickle.read[State](pickleState)
    val me = upickle.read[lagcomp.Peer](pickleMe)
    render(me)(state)
  }
}

// All this ugly imperative stuff make it run at ~60 fps :)
object UglyDomUpdates {
  val blockMap = Array.tabulate[Boolean](World.height, World.width)((_, _) => false)
  
  def showPoint(point: Point, show: Boolean): Unit = {
    if(show != blockMap(point.y)(point.x)) {
      val id = point.x + "-" + point.y
      getElementById(id).style.display = (if(show) "block" else "none")
      blockMap(point.y)(point.x) = show
    }
  }
  
  def showAliveBlocks(bxs: List[Block]): Unit = {
    bxs.foreach { block => showPoint(block.cell, true) }
  }
  
  @scala.annotation.tailrec
  def hideDeadBlocks(a: List[Block], b: List[Block]): Unit = {
    // Equivalent to this without, made faster be
    // a.map(_.cell) diff b.map(_.cell) foreach { block => showPoint(block, false) }
    (a, b) match {
      case (Nil, _) => ()
      case (as :: axs, bs :: bxs) if (as.cell == bs.cell) =>
        hideDeadBlocks(axs, bxs)
      case (as :: axs, _) =>
        showPoint(as.cell, false)
        hideDeadBlocks(axs, b)
    }
  }

  def getElementById(id: String): dom.raw.HTMLElement = {
    dom.document.getElementById(id).asInstanceOf[dom.raw.HTMLElement]
  }

  def adjustView(ship: Ship): Unit = {
    def inBounds(l: Int, u: Int, v: Int): Int = if(v < l) l else if(v > u) u else v

    val w = dom.window

    val upperX = Math.max(0, ship.x.toInt - w.innerWidth / 3)
    val lowerX = Math.min(ship.x.toInt - 2 * w.innerWidth / 3, World.widthPx - w.innerWidth)
    val newX = inBounds(l=lowerX, u=upperX, w.pageXOffset)

    val upperY = Math.max(0, ship.y.toInt - w.innerHeight / 3)
    val lowerY = Math.min(ship.y.toInt - 2 * w.innerHeight / 3, World.heightPx - w.innerHeight)
    val newY = inBounds(l=lowerY, u=upperY, w.pageYOffset)

    w.scrollTo(newX, newY)
  }

  def apply(me: lagcomp.Peer)(state: State): Unit = {
    Option(getElementById("blocks")).foreach { blocksDiv =>
      blocksDiv.className = "frame-" + ((state.time % 128) >> 5)
      showAliveBlocks(state.blocks)
      hideDeadBlocks(World.initialBlocks, state.blocks)
    }
    
    adjustView(if(me == lagcomp.P1) state.ship1 else state.ship2)
  }
}
