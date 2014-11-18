package survivor

import scala.scalajs.js
import org.scalajs.dom

import akka.actor._

import transport._
import transport.akka._
import transport.javascript._

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import models._

object Main extends js.JSApp {
  RegisterPicklers.registerPicklers()

  implicit val system = ActorSystem("client")

  val engine = new Engine(Game.initialState, Game.nextState, Ui.render)
  
  var peer: ActorRef = _
  
  var lastFrameTime: Double = -1.0
  var lastFrameNumber: Int = -1
  
  var lastKey: (Key, Action) = (Space, Release)
  var callId = 0
  
  def main(): Unit = {
    ActorWrapper(new SockJSClient()).connectWithActor(SockJSUrl("http://localhost:9000/sockjs"))(
      ConnectionHandlerActor.props)
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
        val event = Event(Input(key, action, Me), lastFrameNumber + 1)
        peer ! event
        engine.receive(event)
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

class ConnectionHandlerActor(out: ActorRef) extends Actor {
  def receive: Receive = {
    case Connected(peer) =>
      Main.peer = peer
      Main.rafLoop(0)
      
      dom.window.addEventListener("keydown", Main.keyListener(Press) _, false)
      dom.window.addEventListener("keyup", Main.keyListener(Release) _, false)
      
      // context.watch(peer)
      context.become(connected(peer))
  }

  def connected(peer: ActorRef): Receive = {
    case e @ Event(_, _) => Main.engine receive e.copy(input=e.input.copy(player=Him))
    // case Terminated(ref) if ref == peer => context.stop(self)
  }
}
object ConnectionHandlerActor {
  def props(out: ActorRef) = Props(new ConnectionHandlerActor(out))
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
