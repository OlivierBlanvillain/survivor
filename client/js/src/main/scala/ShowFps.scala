package survivor

import org.scalajs.dom

class ShowFps {
  val filterStrength: Int = 20
  var frameTime: Double = 0
  var lastLoop: Double = 0
  
  def apply(currentTime: Double): Unit = {
    var thisFrameTime = currentTime - lastLoop
    frameTime += (thisFrameTime - frameTime) / filterStrength
    lastLoop = currentTime
  }

  var fpsOut = dom.document.getElementById("fps")
  
  dom.window.setInterval(() => fpsOut.innerHTML = (1000 / frameTime).toInt.toString, 1000)
}
