package lagcomp

import scala.concurrent._
import transport.ConnectionHandle
import scala.util._

case class Event[Input](move: Action[Input], time: Int)

sealed trait Peer
case object P1 extends Peer
case object P2 extends Peer

class Engine[Input, State](
      initialState: State,
      nextState: (State, Set[Action[Input]]) => State,
      render: State => Unit,
      broadcast: ConnectionHandle
    )(implicit
      ec: ExecutionContext,
      iw: upickle.Writer[Event[Input]],
      ir: upickle.Reader[Event[Input]]
    ) extends AbstractEngine(initialState, nextState, render, broadcast) {
  
  def triggerRendering(): Unit = {
    clockSync.futureGlobalTime.value match {
      case Some(Success(globalTime)) =>
        render(loop.stateAt(globalTime()))
      case _ =>
        render(loop.stateAt(0))
    }
  }
  def futureAct: Future[Input => Unit] = actPromise.future
  
  private val actPromise = Promise[Input => Unit]()

  val loop = new StateLoop(initialState, nextState)
  val clockSync = new ClockSync(broadcast)
  
  broadcast.handlerPromise.success { pickle =>
    if(clockSync.pending) {
      clockSync.receive(pickle)
    } else {
      loop.receive(upickle.read[Event[Input]](pickle))
    }
  }
  
  for {
    globalTime <- clockSync.futureGlobalTime
    identity <- clockSync.futureIdentity
  } actPromise.success { input =>
    val event = Event(Action(input, identity), globalTime())
    loop.receive(event)
    broadcast.write(upickle.write(event))
  }
}
