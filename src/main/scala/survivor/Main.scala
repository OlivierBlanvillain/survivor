package survivor

import scala.scalajs.js
import org.scalajs.dom
import dom.extensions.KeyCode

object Main extends js.JSApp {
  def main(): Unit = {
    val engine = new GameEngine(Game.initialState, Game.nextState, Ui.render)
    
    val now = getTime().toInt
    dom.window.addEventListener("keydown", keyListener(true, now, engine) _, false)
    dom.window.addEventListener("keyup", keyListener(false, now, engine) _, false)
    
    jsloop(engine)(0)
  }

  def getTime(): Int = new js.Date().getTime().toInt
  def miliToNFrame(t: Int) = t * 6 / 100

  def keyListener(pressed: Boolean, startTime: Int, engine: GameEngine)(e0: dom.Event): Unit = {
    val e = e0.asInstanceOf[dom.KeyboardEvent]
    val optionalKey = e.keyCode match {
      case KeyCode.left => Some(Left)
      case KeyCode.up => Some(Up)
      case KeyCode.right => Some(Right)
      case KeyCode.down => Some(Down)
      case KeyCode.shift => Some(Shift)
      case KeyCode.ctrl => Some(Shift)
      case _ => None
    }
    optionalKey.foreach { key =>
      if(key != Shift) e.preventDefault()
      engine.receive(Event(Input(key, pressed, Me), miliToNFrame(getTime() - startTime)))
    }
  }
  
  
  def jsloop(engine: GameEngine)(currentTime: Double): Unit = {
    engine.loop(miliToNFrame(currentTime.toInt))
    Fps(currentTime)
    dom.window.requestAnimationFrame(jsloop(engine) _)
  }
}

object Fps {
  private val filterStrength: Int = 20
  private var frameTime: Double = 0.0
  private var lastLoop: Double = 0.0
  
  def apply(currentTime: Double): Unit = {
    var thisFrameTime = currentTime - lastLoop
    frameTime += (thisFrameTime - frameTime) / filterStrength
    lastLoop = currentTime
  }

  var fpsOut = dom.document.getElementById("fps")
  dom.window.setInterval({ _: Any =>
    fpsOut.innerHTML = (1000 / frameTime).toInt.toString
  }, 1000)
}
