package survivor

import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.JFXApp
import scalafx.Includes._
import scalafx.scene.input.{ KeyCode, KeyEvent }
import scalafx.scene.layout.BorderPane
import scalafx.scene.Scene
import scalafx.scene.web.WebView

import javafx.beans.value.ChangeListener
import javafx.concurrent.Worker.{ State => WorkerState }
import javafx.beans.value.ObservableValue

import scala.concurrent.ExecutionContext.Implicits.global

import transport._
import transport.webrtc._
import transport.tyrus._

import lagcomp._

object JvmClient extends JFXApp {
  lazy val webView = new WebView()
  lazy val root = new Scene(new BorderPane { center = webView })
  
  val ui = new Ui(webView) 
  
  onLoad(webView) {
    val futureConnection = new WebSocketClient()
      .connect(WebSocketUrl("ws://localhost:8080/ws"))
      .flatMap(new WebRTCSignalingFallback().connect(_))
    
    futureConnection.foreach { connection => 
      val engine = new Engine[Input, State](
        Game.initialState,
        Game.nextState,
        ui.render,
        connection)
      
      engine.futureAct.foreach(new KeyboardListener(_, root))

      new ShowFps(root, webView)
      new GameLoop(engine.triggerRendering _)
    }
  }

  stage = new PrimaryStage {
    title = "Survivor"
    width = 800
    height = 600
    scene = root
  }
  
  def onLoad(webView: WebView)(f: => Unit): Unit = {
    webView.engine.getLoadWorker().stateProperty().addListener(
      new ChangeListener[WorkerState]() {
        def changed(obs: ObservableValue[_ <: WorkerState],
              oldState: WorkerState, newState: WorkerState): Unit = {
          if(newState == WorkerState.SUCCEEDED) f
        }
      }
    )
  }

}

class GameLoop(render: () => Unit) {
  import javafx.animation.AnimationTimer
  
  new AnimationTimer {
    def handle(now: Long): Unit = render()
  }.start()
}

  
class KeyboardListener(act: Input => Unit, scene: Scene) {
  scene.setOnKeyPressed(listener(Press) _)
  scene.setOnKeyReleased(listener(Release) _)

  var lastKey: (Key, KeyAction) = (Space, Release)

  def listener(action: KeyAction)(k: KeyEvent): Unit = {
    val optionalKey = k.code match {
      case KeyCode.SPACE => Some(Space)
      case KeyCode.LEFT => Some(Left)
      case KeyCode.UP => Some(Up)
      case KeyCode.RIGHT => Some(Right)
      case KeyCode.DOWN => Some(Down)
      case _ => None
    }
    optionalKey.foreach { key =>
      if((key, action) != lastKey) {
        lastKey = (key, action)
        act(Input(key, action))
      }
    }
  }
}

class Ui(webView: WebView) {
  import upickle._
  import scalatags.Text.all._

  def render(state: State): Unit = {
    val pickledState = upickle.write(state)
    webView.engine.executeScript(s"""ReactUi().renderString('$pickledState');""")
  }
  
  val projectRoot = "file://" + System.getProperty("user.dir")
  
  webView.engine.loadContent("<!DOCTYPE>" + html(
    head(link(rel:="stylesheet", href:=projectRoot+"/assets/css/survivor.css")),
    body(
      div(id:="world"),
      div(id:="stats", "FPS: ", span(id:="fps", "N/A")),
      script(src:="http://cdnjs.cloudflare.com/ajax/libs/react/0.12.1/react.min.js"),
      script(src:=projectRoot+"/client/js/target/scala-2.11/survivorjs-fastopt.js")
    )
  ).toString)
}
