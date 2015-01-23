package lagcomp

import scala.concurrent._
import transport.ConnectionHandle
import scala.util.Random

class ClockSync(connection: ConnectionHandle, localTime: () => Long) {
  var behavior: (String => Unit) = greeting _

  private val globalTimePromise = Promise[() => Int]()
  private val identityPromise = Promise[Peer]()
  
  def futureGlobalTime: Future[() => Int] = globalTimePromise.future
  def futureIdentity: Future[Peer] = identityPromise.future
  def pending: Boolean = !globalTimePromise.isCompleted

  val localRandom = Random.nextLong()
  connection.write(localRandom.toString)
  
  def receive(pickle: String): Unit = {
    behavior(pickle)
  }

  def greeting(pickle: String): Unit = {
    val remoteRandom = pickle.toLong
    if(localRandom < remoteRandom) {
      behavior = askingTime(localTime()) _
      connection.write("")
    } else {
      behavior = givingTime _
    }
  }
  
  def givingTime(pickle: String): Unit = {
    val now = localTime()
    connection.write(now.toString)
    identityPromise.success(P2)
    globalTimePromise.success(() => (localTime() - now + 500).toInt * 60 / 1000)
  }
  
  def askingTime(askedAt: Long)(pickle: String): Unit = {
    val now = localTime()
    val tripTime = (now - askedAt) / 2
    identityPromise.success(P1)
    globalTimePromise.success(() => (localTime() - now + tripTime + 500).toInt * 60 / 1000)
  }
}
