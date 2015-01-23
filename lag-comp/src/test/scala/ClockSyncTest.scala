package test

import scala.concurrent._
import scala.concurrent.duration._
import minitest._
import transport._
import transport.ConnectionHandle
import lagcomp.ClockSync

import scala.concurrent.ExecutionContext.Implicits.global

object ClockSyncTest extends SimpleTestSuite {
  test("should sync") {
    
    val localClock1 = () => System.currentTimeMillis() + 1000
    val localClock2 = () => System.currentTimeMillis() - 1000

    val (connection1, connection2) = ProxyConnectionHandle.newConnectionsPair()
    
    val clockSync1 = new ClockSync(connection1, localClock1)
    val clockSync2 = new ClockSync(connection2, localClock2)
    
    connection1.handlerPromise.success { m =>
      clockSync1.receive(m)
    }
    connection2.handlerPromise.success { m =>
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
        assertEquals(t1, t2)
    }
  }
}
