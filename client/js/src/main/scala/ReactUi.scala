package survivor

import scalajs.js
import js.annotation.JSExport
import org.scalajs.dom

import upickle._

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import scala.util.Random

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
      val frame =
        if(deadTime % 8 == 3 || deadTime % 8 == 4) 0
        else if(deadTime > 0.5*respawnDelay) 0
        else if(deadTime > 0.4*respawnDelay) 4
        else if(deadTime > 0.3*respawnDelay) 3
        else if(deadTime > 0.2*respawnDelay) 2
        else if(deadTime > 0.1*respawnDelay) 1
        else 3
      
      div(
        for(i <- List(-1, 0, 1); j <- List(-1, 0, 1) if(!(i == 0 && j == 0))) yield
          div(cls:=s"big-explosion-$frame", top:=y+8*deadTime*i, left:=x+8*deadTime*j)
      )()
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
  
  val gunfires = component[(List[Gunfire], Int)] { case (gfs, now) =>
    div(gfs.map { gunfire =>
      div(cls:="ship-gunfire", top:=gunfire.y(now), left:=gunfire.x(now))
    })
  }
  
  val world = component[State] { s =>
    div(ship((s.myShip, s.time)), ship((s.hisShip, s.time)), gunfires((s.gunfires, s.time)))
  }

  def render(state: State): Unit = {
    world(state) render dom.document.getElementById("world")
  }
  
  @JSExport("renderString")
  def renderString(string: String): Unit = {
    val state = upickle.read[State](string)
    render(state)
  }
}
