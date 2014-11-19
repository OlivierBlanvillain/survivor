package survivor

import scalajs.js
import org.scalajs.dom

import japgolly.scalajs.react._ 
import vdom.ReactVDom._         
import vdom.ReactVDom.all._     

object Ui {
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
      xOrientation == ⇦ option "thrust-left",
      xOrientation == ⇨ option "thrust-right",
      yOrientation == ⇧ option "thrust-up",
      yOrientation == ⇩ option "thrust-down",
      dying option "hidden"
    ) flatMap (_.toList) mkString " "

    div(cls:=clazz, top:=yPosition, left:=xPosition)()
  }
    
  val world = component[State] { s =>
    div(ship(s.myShip), ship(s.hisShip))
  }

  def render(state: State): Unit = {
    // System.err.println(state.ship.toString)
    world(state) render dom.document.getElementById("world")
  }
}