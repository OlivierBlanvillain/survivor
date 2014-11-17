package survivor

import scala.scalajs.js
import org.scalajs.dom

object Main extends js.JSApp {
  val engine = new Engine(Game.initialState, Game.nextState, Ui.render)
  
  var lastFrameTime: Double = -1.0
  var lastFrameNumber: Int = -1
  
  var lastKey: (Key, Action) = (Space, Release)
  var callId = 0
  
  def main(): Unit = {
    System.out.println("Survivor")
   
    rafLoop(0)
    
    dom.window.addEventListener("keydown", keyListener(Press) _, false)
    dom.window.addEventListener("keyup", keyListener(Release) _, false)
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
        engine.receive(Event(Input(key, action), lastFrameNumber + 1, Me))
      }
    }
  }
  
  def rafLoop(currentTime: Double): Unit = {
    dom.window.clearInterval(callId)
    callId = dom.window.setInterval(() => engineLoop(this.lastFrameTime + 1000), 1000)
    engineLoop(currentTime)
    ShowFps(currentTime)
    dom.window.requestAnimationFrame(rafLoop _)
  }

  def engineLoop(currentTime: Double): Unit = {
    val beforeLastFrameNumber = lastFrameNumber
    lastFrameTime = currentTime
    lastFrameNumber = currentTime.toInt * 60 / 1000
    if(lastFrameNumber != beforeLastFrameNumber) {
      engine.loop(lastFrameNumber)
    }
  }
}

object ShowFps {
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
