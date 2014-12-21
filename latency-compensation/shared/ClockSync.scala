package lagcomp

import scala.concurrent._
import transport.ConnectionHandle

class ClockSync(connection: ConnectionHandle) {
  var pending: Boolean = true

  private val globalTimePromise = Promise[() => Int]()
  private val identityPromise = Promise[Peer]()
  
  def futureGlobalTime: Future[() => Int] = globalTimePromise.future
  def futureIdentity: Future[Peer] = identityPromise.future

  val selfStartTime = System.currentTimeMillis()
  connection.write(selfStartTime.toString)
    
  def receive(pickle: String): Unit = {
    val remoteStartTime = pickle.toLong
    val startTime = Math.max(selfStartTime, remoteStartTime)
    
    identityPromise.success(if(selfStartTime < remoteStartTime) P1 else P2)
    globalTimePromise.success(() => (System.currentTimeMillis() - startTime).toInt * 60 / 1000)
    pending = false
  }
}
