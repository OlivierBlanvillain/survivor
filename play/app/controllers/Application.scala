package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka.system
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import actors._
import models._

import transport.play._
import transport.akka._

object Application extends Controller {
  RegisterPicklers.registerPicklers()
  
  implicit val implicitSystem = system
  
  val peerMatcher = system.actorOf(PeerMatcher.props)
  val sockJStransport = new SockJSServer()
  val sockJS = sockJStransport.action()
  
  ActorWrapper(sockJStransport).acceptWithActor(UserActor.props(peerMatcher))
}
