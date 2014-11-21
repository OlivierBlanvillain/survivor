package survivor

import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.web.WebView

import java.util.{ Timer, TimerTask }
import com.sun.javafx.perf.PerformanceTracker

class ShowFps(scene: Scene, webView: WebView) {
  new Timer().scheduleAtFixedRate(new TimerTask {
    def run: Unit = {
      Platform.runLater {
        val fps = PerformanceTracker.getSceneTracker(scene).getInstantFPS().toInt
        webView.engine.executeScript(s"""document.getElementById("fps").innerHTML = "$fps";""")
        ()
      }
    }
  }, 0, 1000)
}
