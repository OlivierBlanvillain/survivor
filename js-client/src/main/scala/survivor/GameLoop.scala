package survivor

import org.scalajs.dom

class GameLoop(loop: () => Unit) {
  
  val AFK_FRAME_INTERVAL = 1000
  var callId: Int = 0

  def rafLoop(t0: Double): Unit = {
    dom.window.clearInterval(callId)
    callId = dom.window.setInterval(loop, AFK_FRAME_INTERVAL)
    loop()
    ShowFps(t0)
    dom.window.requestAnimationFrame(rafLoop _)
  }
  
  rafLoop(0)
  
}
