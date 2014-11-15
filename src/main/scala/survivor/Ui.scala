package survivor

import scalajs.js
import org.scalajs.dom

import japgolly.scalajs.react._ 
import vdom.ReactVDom._         
import vdom.ReactVDom.all._     

object Ui {
  /** Same sementic as the macro defined in [[https://github.com/levand/quiescent Quiescent]]. */
  def Component[T <: AnyRef](r: T => VDom) = ReactComponentB[T]("")
    .render(r)
    .shouldComponentUpdate((scope, props, _) => scope.props ne props)
    .build
  
  val ship = Component[Ship] { s =>
    div(
      `class`:="ship",
      top:=s.yPosition,
      left:=s.xPosition
    )()}
    
  val world = Component[State] { s => ship(s.ship) }

    // thrust: {
    //   up: 'thrust-up',
    //   right: 'thrust-right',
    //   down: 'thrust-down',
    //   left: 'thrust-left'
    // },
    // thrusting: 'thrusting',
    // hidden: 'hidden'
  
  def render(state: State): Unit = {
    System.err.println(state.ship.toString)
    world(state) render dom.document.getElementById("world")
  }
}
