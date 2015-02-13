package survivor

object JsClient extends scala.scalajs.js.JSApp {
  import scalajs.concurrent.JSExecutionContext.Implicits.runNow
  import transport._
  import transport.webrtc._
  import transport.javascript._
  import lagcomp._
  
  def main(): Unit = {
    val futureConnection = new WebSocketClient()
      // .connect(WebSocketUrl("wss://sleepy-atoll-7403.herokuapp.com/ws"))
      .connect(WebSocketUrl("ws://localhost:8080/ws"))
      .flatMap(new WebRTCClientFallback().connect(_))
    
    futureConnection.foreach { connection => 
      val engine = new Engine[Input, State](
        World.initialState,
        Game.nextState,
        ReactUi.render,
        connection)
      
      engine.futureAct.foreach(new KeyboardListener(_))
      
      new GameLoop(engine.triggerRendering _)
    }
  }
}

class GameLoop(render: () => Unit) {
  import org.scalajs.dom
  
  val AFK_FRAME_INTERVAL = 1000.0
  var callId: Int = 0

  def rafLoop(t0: Double): Unit = {
    dom.window.clearInterval(callId)
    callId = dom.window.setInterval(render, AFK_FRAME_INTERVAL)
    render()
    dom.window.requestAnimationFrame(rafLoop _)
  }
  
  rafLoop(0)
}

class KeyboardListener(act: Input => Unit) {
  import org.scalajs.dom
  import dom.ext.KeyCode
  
  var lastKey: (Key, KeyAction) = (Space, Release)
  
  dom.window.addEventListener("keydown", listener(Press) _, false)
  dom.window.addEventListener("keyup", listener(Release) _, false)

  def listener(action: KeyAction)(e: dom.Event): Unit = {
    val domEvent = e.asInstanceOf[dom.KeyboardEvent]
    val optionalKey = domEvent.keyCode match {
      case 32 => Some(Space)
      case KeyCode.left => Some(Left)
      case KeyCode.up => Some(Up)
      case KeyCode.right => Some(Right)
      case KeyCode.down => Some(Down)
      case _ => None
    }
    optionalKey.foreach { key =>
      domEvent.preventDefault()
      if((key, action) != lastKey) {
        lastKey = (key, action)
        act(Input(key, action))
      }
    }
  }
}
