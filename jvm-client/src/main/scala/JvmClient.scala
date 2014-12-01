package survivor

import scalafx.animation.{ KeyFrame, Timeline }
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.JFXApp
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.scene.input.{ KeyCode, KeyEvent }
import scalafx.scene.layout.BorderPane
import scalafx.scene.Scene
import scalafx.scene.web.WebView

import javafx.beans.value.ChangeListener
import javafx.concurrent.Worker.{ State => WorkerState }
import javafx.beans.value.ObservableValue

import scala.concurrent.ExecutionContext.Implicits.global

import transport.tyrus._

object JvmClient extends JFXApp {
  lazy val webView = new WebView()
  lazy val root = new Scene(new BorderPane { center = webView })
  
  val ui = new Ui(webView) 
  
  onLoad(webView) {
    val engine = new Engine(Game.initialState, Game.nextState, ui.render)
    val transport = new WebSocketClient()
    
    new Network(transport, engine).onConnected { case (fireEvent, gameTime) =>
      new ShowFps(root, webView)
      new GameLoop(engine, gameTime)
      new KeyboardListener(fireEvent, gameTime, root)
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

class GameLoop(engine: Engine, gameTime: () => Int) {
  val keyFrame = KeyFrame(16.ms, onFinished = { _: ActionEvent =>
    engine.loop(gameTime())
  })
  
  new Timeline {
    keyFrames = Seq(keyFrame)
    cycleCount = Timeline.INDEFINITE
  }.play
}

  
class KeyboardListener(fireEvent: (Player => Event) => Unit, getFrame: () => Int, scene: Scene) {
  scene.setOnKeyPressed(listener(Press) _)
  scene.setOnKeyReleased(listener(Release) _)

  var lastKey: (Key, Action) = (Space, Release)

  def listener(action: Action)(k: KeyEvent): Unit = {
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
        fireEvent(me => Event(Input(key, action, me), getFrame() + 1))
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
      script(src:=projectRoot+"/react-ui/target/scala-2.11/reactui-jsdeps.js"),
      script(src:=projectRoot+"/react-ui/target/scala-2.11/reactui-fastopt.js")
    )
  ).toString)
}
