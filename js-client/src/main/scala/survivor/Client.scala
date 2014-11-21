package survivor

import scala.scalajs.js.JSApp

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import transport.javascript._

object Client extends JSApp {
  def main(): Unit = {
    new Main(new WebSocketClient())
  }
}
