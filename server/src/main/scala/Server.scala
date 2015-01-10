package server

import transport._
import transport.netty._

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends App {
  
  var pendingConnection: Option[ConnectionHandle] = None
  
  
  println("Starting Up...")
  val netty = new WebSocketServer(8080, "/ws")
  netty.listen().foreach { promise =>
    println()
    println("Press enter to interrupt")
    
    promise.success { inboundConnection =>
      pendingConnection = pendingConnection match {
        case None =>
          println("pending...")
          Some(inboundConnection)
        case Some(connection) =>
          println("Connected!")
          connection.handlerPromise.success { m =>
            println(s"NEW -> OLD: $m")
            inboundConnection.write(m)
          }
          inboundConnection.handlerPromise.success { m =>
            println(s"OLD -> NEW: $m")
            connection.write(m)
          }
          None
      }
    }
  }
  
  System.in.read()
  
  netty.shutdown()
}
