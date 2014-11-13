package survivor

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.extensions.KeyCode

object Main extends js.JSApp {
  var inputs: List[Event] = List()
  
  def main(): Unit = {
    loop(0)
  }
  
  def loop(currentTime: Double): Unit = {
    val time = currentTime.toInt
    val state = Game.computeState(inputs, time)
    Ui.render(state, time)

    Fps(currentTime)
    dom.window.requestAnimationFrame(loop _)
  }
}
