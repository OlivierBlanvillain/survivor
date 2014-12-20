package lagcomp

import scala.concurrent.{ ExecutionContext, Promise }
import transport._
import upickle._

class Network(connection: Future[ConnectionHandle], engine: Engine)(implicit ec: ExecutionContext) {
  var initialize = false

  val url = WebSocketUrl("ws://localhost:8080/ws")
  // val engine = new Engine(Game.initialState, Game.nextState, Ui.render)
  
  val promise = Promise[((Peer => Event) => Unit, () => Int)]()
  
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
        def gameTime(): Int = (System.currentTimeMillis() - startTime).toInt * FPS / 1000
        def fireEvent(anonymousEvent: Peer => Event): Unit = {
          val event = anonymousEvent(me)
          engine.receive(event)
          connection.write(upickle.write(event))
        }
        
        promise.success((fireEvent _, gameTime _))

      } 
    }
    
  }
  
  def onConnected(callback: (((Peer => Event) => Unit, () => Int)) => Unit): Unit = {
    promise.future foreach callback
  }
}
