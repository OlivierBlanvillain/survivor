package survivor

import scala.scalajs.js
import org.scalajs.dom

import upickle._

import transport._
import transport.javascript._

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

object Client extends js.JSApp {
  val afkFps = 1
  val fps = 60
  
  var lastFrameTime: Double = -1.0
  def lastFrameNumber = lastFrameTime.toInt * fps / 1000
  
  var lastKey: (Key, Action) = (Space, Release)
  var callId = 0
  
  def main(): Unit = {
    val url = WebSocketUrl("ws://localhost:8080/ws")
    val engine = new Engine(Game.initialState, Game.nextState, Ui.render)

    if(dom.document.URL contains "solo") {
      startGame(engine.receive(_), engine.loop _)
    } else {
      new WebSocketClient().connect(url) foreach { connection =>
        def fireEvent(event: Event): Unit = {
          connection.write(upickle.write(event))
          engine.receive(event)
        }
        
        connection.handlerPromise.success { pickle =>
          val event = upickle.read[Event](pickle)
          engine.receive(event.copy(input=event.input.copy(player=Him)))
        }
        
        fireEvent(Event(Input(key, action, Me), lastFrameNumber + 1))
        
        startGame(fireEvent _, engine.loop _)

      }
    }
  }
  
  def startGame(fireEvent: Event => Unit, engineLoop: Int => Unit): Unit = {
    rafLoop(engineLoop)(0)
    dom.window.addEventListener("keydown", keyListener(fireEvent, Press) _, false)
    dom.window.addEventListener("keyup", keyListener(fireEvent, Release) _, false)
  }
  
  def keyListener(fireEvent: Event => Unit, action: Action)(e0: dom.Event): Unit = {
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
        fireEvent(event)
      }
    }
  }
  
  def rafLoop(engineLoop: Int => Unit)(currentTime: Double): Unit = {
    dom.window.clearInterval(callId)
    callId = dom.window.setInterval({ () =>
      lastFrameTime += 1000 / afkFps
      engineLoop(lastFrameNumber)
    }, 1000 / afkFps)
    
    lastFrameTime = currentTime
    engineLoop(lastFrameNumber)
    
    ShowFps(currentTime)
    dom.window.requestAnimationFrame(rafLoop(engineLoop) _)
  }

}
