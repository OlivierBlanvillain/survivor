package survivor

import scala.scalajs.js
import org.scalajs.dom

import upickle._

import transport._
import transport.javascript._

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

object Main extends js.JSApp {
  val url = WebSocketUrl("ws://localhost:8080/ws")
  val afkFps = 1
  val fps = 60
  val engine = new Engine(Game.initialState, Game.nextState, Ui.render)
  
  var lastFrameTime: Double = -1.0
  def lastFrameNumber = lastFrameTime.toInt * fps / 1000
  
  var lastKey: (Key, Action) = (Space, Release)
  var callId = 0
  
  def main(): Unit = {
    if(dom.document.URL contains "solo") {
      startGame(_ => ())
    } else {
      
      new WebSocketClient().connect(url).foreach { connection =>
        connection.handlerPromise.success { pickle =>
          val event = upickle.read[Event](pickle)
          engine.receive(event.copy(input=event.input.copy(player=Him)))
        }
        startGame(event => connection.write(upickle.write(event)))
      }
    }
  }
  
  def startGame(sentToPeer: Event => Unit): Unit = {
    rafLoop(0)
    dom.window.addEventListener("keydown", keyListener(sentToPeer, Press) _, false)
    dom.window.addEventListener("keyup", keyListener(sentToPeer, Release) _, false)
  }
  
  def keyListener(sentToPeer: Event => Unit, action: Action)(e0: dom.Event): Unit = {
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
        sentToPeer(event)
        engine.receive(event)
      }
    }
  }
  
  def rafLoop(currentTime: Double): Unit = {
    dom.window.clearInterval(callId)
    callId = dom.window.setInterval({ () =>
      lastFrameTime += 1000 / afkFps
      engine.loop(lastFrameNumber)
    }, 1000 / afkFps)
    
    lastFrameTime = currentTime
    engine.loop(lastFrameNumber)
    
    ShowFps(currentTime)
    dom.window.requestAnimationFrame(rafLoop _)
  }

}
