package survivor

import scala.scalajs.js
import org.scalajs.dom

object Main extends js.JSApp {
  var currentFrame = 0
  var lastKey: (Key, Action) = (Space, Release)
  val engine = new Engine(Game.initialState, Game.nextState, Ui.render)
  
  def main(): Unit = {
    System.out.println("Survivor")
    dom.window.addEventListener("keydown", keyListener(Press) _, false)
    dom.window.addEventListener("keyup", keyListener(Release) _, false)
    jsloop(0)
  }

  def keyListener(action: Action)(e0: dom.Event): Unit = {
    val e = e0.asInstanceOf[dom.KeyboardEvent]
    val optionalKey = e.keyCode match {
      case 32 => Some(Space)
      case 37 => Some(Left)
      case 38 => Some(Up)
      case 39 => Some(Right)
      case 40 => Some(Down)
      case _ => None
    }
    optionalKey.foreach { key =>
      e.preventDefault()
      if((key, action) != lastKey) {
        lastKey = (key, action)
        engine.receive(Event(Input(key, action), currentFrame, Me))
      }
    }
  }
  
  
  def jsloop(currentTime: Double): Unit = {
    currentFrame = currentTime.toInt * 6 / 100
    engine.loop(currentFrame)
    Fps(currentTime)
    dom.window.requestAnimationFrame(jsloop _)
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
