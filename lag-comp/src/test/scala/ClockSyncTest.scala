package test

import scala.concurrent._
import scala.concurrent.duration._
import minitest._
import transport._
import transport.ConnectionHandle
import lagcomp._

import scala.concurrent.ExecutionContext.Implicits.global

object ClockSyncTest extends SimpleTestSuite {
  test("should sync") {
    var timePassed = 0
    def getTimePassed() = timePassed
    def timePasses() = timePassed = timePassed + 500
    
    val localClock1 = () => getTimePassed() + System.currentTimeMillis() + 10000
    val localClock2 = () => getTimePassed() + System.currentTimeMillis() - 10000

    val (connection1, connection2) = ProxyConnectionHandle.newConnectionsPair()
    val clockSync1 = new ClockSync(connection1, localClock1)
    val clockSync2 = new ClockSync(connection2, localClock2)
    connection1.handlerPromise.success { m =>
      timePasses()
      clockSync1.receive(m)
    }
    connection2.handlerPromise.success { m =>
      timePasses()
      clockSync2.receive(m)
    }
    
    val globalTimes = for {
      globalTime1 <- clockSync1.futureGlobalTime
      globalTime2 <- clockSync2.futureGlobalTime
    } yield (globalTime1, globalTime2)
    
    Await.result(globalTimes, 1.seconds) match {
      case (globalTime1, globalTime2) =>
        val t1: Int = globalTime1()
        val t2: Int = globalTime2()
        assert(-20 to 20 contains (t1 - t2))
    }
  }
  
  test("should set peer identity") {
    val (connection1, connection2) = ProxyConnectionHandle.newConnectionsPair()
    val clockSync1 = new ClockSync(connection1, System.currentTimeMillis)
    val clockSync2 = new ClockSync(connection2, System.currentTimeMillis)
    
    assert(clockSync1.pending && clockSync2.pending)

    connection1.handlerPromise.success(clockSync1.receive)
    connection2.handlerPromise.success(clockSync2.receive)
    
    val ids = for {
      id1 <- clockSync1.futureIdentity
      id2 <- clockSync2.futureIdentity
    } yield (id1, id2)
    
    Await.result(ids, 1.seconds) match {
      case (id1, id2) =>
        assert(!clockSync1.pending && !clockSync2.pending)
        assert((id1 == P1 && id2 == P2) || (id1 == P2 && id2 == P1))
    }
  }
}
