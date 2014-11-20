package survivor

import System.currentTimeMillis

import scala.scalajs.js
import org.scalajs.dom

import upickle._

import transport._
import transport.javascript._

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

object Client extends js.JSApp {
  val afkFps = 1
  val fps = 60

  var lastFrameTime: Long = -1
  def lastFrameNumber = lastFrameTime.toInt * fps / 1000
  var lastKey: (Key, Action) = (Space, Release)
  var callId = 0
  var first = true
  var startTime: Long = _
  
  def main(): Unit = {
    val url = WebSocketUrl("ws://localhost:8080/ws")
    val engine = new Engine(Game.initialState, Game.nextState, Ui.render)

    if(dom.document.URL contains "solo") {
      startGame(e => engine.receive(e(P1)), engine.loop _)
    } else {
      new WebSocketClient().connect(url) foreach { connection =>
        val selfStartTime = currentTimeMillis()
        connection.write(selfStartTime.toString)

        connection.handlerPromise.success { pickle =>
          if(first) {
            first = false
            val remoteStartTime = pickle.toLong
            startTime = Math.max(selfStartTime, remoteStartTime) + 1000
            
            def fireEvent(e: Player => Event): Unit = {
              val event = if(selfStartTime < remoteStartTime) e(P1) else e(P2)
              connection.write(upickle.write(event))
              engine.receive(event)
            }
            
            dom.window.setTimeout({ () =>
              startGame(fireEvent, engine.loop _)
            }, startTime - currentTimeMillis())

          } else {
            engine.receive(upickle.read[Event](pickle))
          }
        }
        
      }
    }
  }
  
  def startGame(fireEvent: (Player => Event) => Unit, engineLoop: Int => Unit): Unit = {
    rafLoop(engineLoop)(0)
    dom.window.addEventListener("keydown", keyListener(fireEvent, Press) _, false)
    dom.window.addEventListener("keyup", keyListener(fireEvent, Release) _, false)
  }
  
  def keyListener(fireEvent: (Player => Event) => Unit, action: Action)(e0: dom.Event): Unit = {
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
        fireEvent(p => Event(Input(key, action, p), lastFrameNumber + 1))
      }
    }
  }
  
  def rafLoop(engineLoop: Int => Unit)(t0: Double): Unit = {
    val currentTime = currentTimeMillis() - startTime
    dom.window.clearInterval(callId)
    callId = dom.window.setInterval({ () =>
      lastFrameTime += 1000 / afkFps
      engineLoop(lastFrameNumber)
    }, 1000 / afkFps)
    
    lastFrameTime = currentTime
    engineLoop(lastFrameNumber)
    
    ShowFps(t0)
    dom.window.requestAnimationFrame(rafLoop(engineLoop) _)
  }

}
