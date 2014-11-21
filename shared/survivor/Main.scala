package survivor

import scala.concurrent.ExecutionContext
import transport._
import upickle._

class Main(transport: WebSocketTransport)(implicit ec: ExecutionContext) {
  var initialize = false

  val url = WebSocketUrl("ws://localhost:8080/ws")
  val engine = new Engine(Game.initialState, Game.nextState, Ui.render)
  
  transport.connect(url) foreach { connection =>

    val selfStartTime = System.currentTimeMillis()
    connection.write(selfStartTime.toString)

    connection.handlerPromise.success { pickle =>
      println("Got: " + pickle)
      if(initialize)
        engine.receive(upickle.read[Event](pickle))
      else {
        
        initialize = true
        
        val remoteStartTime = pickle.toLong
        val startTime = Math.max(selfStartTime, remoteStartTime)
        val me = if(selfStartTime < remoteStartTime) P1 else P2
        
        val FPS = 60
        def getFrame(): Int = (System.currentTimeMillis() - startTime).toInt * FPS / 1000
        def fireEvent(event: Event): Unit = {
          engine.receive(event)
          connection.write(upickle.write(event))
        }

        new GameLoop(() => engine.loop(getFrame()))
        new KeyboardListener(fireEvent _, getFrame _, me)

      } 
    }
    
  }
}