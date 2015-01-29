package survivor

import scalajs.js
import js.annotation.JSExport
import org.scalajs.dom

import upickle._

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import scala.util.Random
import lagcomp.{ Peer, P1, P2 }

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
          else if(deadTime > 0.4*respawnDelay) 4
          else if(deadTime > 0.3*respawnDelay) 3
          else if(deadTime > 0.2*respawnDelay) 2
          else if(deadTime > 0.1*respawnDelay) 1
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
  
  val gunfires = component[List[Gunfire]] { gfs =>
    div(gfs.map { gunfire =>
      div(cls:="ship-gunfire", top:=gunfire.y, left:=gunfire.x)
    })
  }

  val blocks = component[(List[Block], Int)] { case (bs, now) =>
    val w = org.scalajs.dom.window
    val innerWidth = w.innerWidth
    val innerHeight = w.innerHeight
    val pageXOffset = w.pageXOffset
    val pageYOffset = w.pageYOffset

    div(bs
          .map { block =>
      if(block dead now) {
        div()
      } else {
        val clazz = if(block exploding now) {
          "block exploding-" + ((now % 16) >> 1)
        } else {
          val frame = ((now % 128) >> 5)
          (block.x < 1000, block.y < 700) match {
            case (true, true) => "block type-0-" + frame
            case (true, false) => "block type-1-" + frame
            case (false, true) => "block type-3-" + frame
            case (false, false) => "block type-2-" + frame
          }
        }
        div(cls:=clazz, top:=block.y-World.unitPx/2, left:=block.x-World.unitPx/2)
      }
    })
  }
  
  val world = component[State] { s =>
    val ship1Component = ship((s.ship1, s.time))
    val ship2Component = ship((s.ship2, s.time))
    val gunfiresComponent = gunfires(s.gunfires)
    val blocksComponent = blocks((s.blocks, s.time))

    div(
      ship1Component,
      ship2Component,
      gunfiresComponent,
      blocksComponent
    )
  }
  
  def adjustView(ship: Ship): Unit = {
    def inBounds(l: Int, u: Int, v: Int): Int = if(v < l) l else if(v > u) u else v

    val w = org.scalajs.dom.window

    val upperX = Math.max(0, ship.x.toInt - w.innerWidth / 3)
    val lowerX = Math.min(ship.x.toInt - 2 * w.innerWidth / 3, World.widthPx - w.innerWidth)
    val newX = inBounds(l=lowerX, u=upperX, w.pageXOffset)

    val upperY = Math.max(0, ship.y.toInt - w.innerHeight / 3)
    val lowerY = Math.min(ship.y.toInt - 2 * w.innerHeight / 3, World.heightPx - w.innerHeight)
    val newY = inBounds(l=lowerY, u=upperY, w.pageYOffset)

    w.scrollTo(newX, newY)
  }
  
  def render(me: Peer)(state: State): Unit = {
    adjustView(if(me == P1) state.ship1 else state.ship2)
    world(state) render dom.document.getElementById("world")
  }
  
  @JSExport
  def renderString(pickleMe: String, pickleState: String): Unit = {
    val state = upickle.read[State](pickleState)
    val me = upickle.read[Peer](pickleMe)
    render(me)(state)
  }
}
  
