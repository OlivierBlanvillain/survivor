package survivor

object JsClient extends scala.scalajs.js.JSApp {
  import scalajs.concurrent.JSExecutionContext.Implicits.runNow
  import transport.javascript._
  
  def main(): Unit = {
    val engine = new Engine(Game.initialState, Game.nextState, ReactUi.render)
    val transport = new WebSocketClient()
    
    new Network(transport, engine).onConnected { case (fireEvent, gameTime) =>
      new GameLoop(engine, gameTime)
      new KeyboardListener(fireEvent, gameTime)
    }
  }
}

class GameLoop(engine: Engine, gameTime: () => Int) {
  import org.scalajs.dom
  
  val AFK_FRAME_INTERVAL = 1000
  val showfps = new ShowFps
  var callId: Int = 0

  def rafLoop(t0: Double): Unit = {
    val loop = () => engine.loop(gameTime())
    dom.window.clearInterval(callId)
    callId = dom.window.setInterval(loop, AFK_FRAME_INTERVAL)
    loop()
    showfps(t0)
    dom.window.requestAnimationFrame(rafLoop _)
  }
  
  rafLoop(0)
}

class KeyboardListener(fireEvent: (Player => Event) => Unit, getFrame: () => Int) {
  import org.scalajs.dom
  import dom.extensions.KeyCode
  
  var lastKey: (Key, Action) = (Space, Release)
  
  dom.window.addEventListener("keydown", listener(Press) _, false)
  dom.window.addEventListener("keyup", listener(Release) _, false)

  def listener(action: Action)(e: dom.Event): Unit = {
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
        fireEvent(me => Event(Input(key, action, me), getFrame() + 1))
      }
    }
  }
}
