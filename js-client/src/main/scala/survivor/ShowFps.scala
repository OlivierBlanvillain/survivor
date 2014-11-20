package survivor

import org.scalajs.dom

object ShowFps {
  private val filterStrength: Int = 20
  private var frameTime: Double = 0
  private var lastLoop: Double = 0
  
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
