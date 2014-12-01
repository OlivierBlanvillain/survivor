package survivor

import scalajs.js
import js.annotation.JSExport
import org.scalajs.dom

import upickle._

import japgolly.scalajs.react._ 
import vdom.ReactVDom._         
import vdom.ReactVDom.all._     

@JSExport
object ReactUi {
  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    final def option[A](a: => A): Option[A] = if (b) Some(a) else None
  }

  def component[T <: AnyRef](r: T => VDom) = ReactComponentB[T]("")
    .render(r)
    .shouldComponentUpdate((scope, props, _) => scope.props ne props)
    .build
    

  val ship = component[Ship] { s =>
    import s._
    
    val clazz = List(
      Some("ship"),
      thrusting option "thrusting",
      xOr == ⇦ option "thrust-left",
      xOr == ⇨ option "thrust-right",
      yOr == ⇧ option "thrust-up",
      yOr == ⇩ option "thrust-down",
      dying option "hidden"
    ) flatMap (_.toList) mkString " "

    div(cls:=clazz, top:=yPos, left:=xPos)()
  }
  
  val gunfires = component[(List[Gunfire], Int)] { case (gfs, now) =>
    div(gfs.map { gunfire =>
      div(cls:="ship-gunfire", top:=gunfire.yPos(now), left:=gunfire.xPos(now))
    })
  }
  
  val world = component[State] { s =>
    div(ship(s.myShip), ship(s.hisShip), gunfires((s.gunfires, s.time)))
  }

  def render(state: State): Unit = {
    world(state) render dom.document.getElementById("world")
  }
  
  @JSExport
  def renderString(string: String): Unit = {
    val state = upickle.read[State](string)
    render(state)
  }
}
