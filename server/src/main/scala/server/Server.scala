package server

import transport._
import transport.netty._

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends App {
  
  var pendingConnection: Option[ConnectionHandle] = None
  
  val netty = new WebSocketServer(8080, "/ws")
  netty.listen().foreach(_.success { inboundConnection =>
    pendingConnection = pendingConnection match {
      case None =>
        Some(inboundConnection)
      case Some(connection) =>
        connection.handlerPromise.success(inboundConnection.write(_))
        inboundConnection.handlerPromise.success(connection.write(_))
        None
    }
  })
  
  System.in.read()
  
  netty.shutdown()
}
