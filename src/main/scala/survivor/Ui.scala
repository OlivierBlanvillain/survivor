package survivor

import scalajs.js
import org.scalajs.dom

import japgolly.scalajs.react._ 
import vdom.ReactVDom._         
import vdom.ReactVDom.all._     

object Ui {
  def referenceEqualityTest(scope: ComponentScopeM[State, _, _], props: State, b: Unit): Boolean =
    scope.props ne props
    
  val world = ReactComponentB[State]("state")
    .render(state => div("State =  ", state.toString))
    .shouldComponentUpdate(referenceEqualityTest _)
    .build
  
  def render(state: State): Unit = {
    world(state) render dom.document.getElementById("world")
  }
}
