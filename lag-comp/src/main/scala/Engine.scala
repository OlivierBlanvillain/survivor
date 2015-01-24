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
      render: Peer => State => Unit,
      broadcast: ConnectionHandle
    )(implicit
      ec: ExecutionContext,
      iw: upickle.Writer[Event[Input]],
      ir: upickle.Reader[Event[Input]]
    ) extends AbstractEngine(initialState, nextState, render, broadcast) {
  
  def triggerRendering(): Unit = {
    // Not with a for loop to ensure that render is called in the same thread
    (clockSync.futureGlobalTime.value, clockSync.futureIdentity.value) match{
      case (Some(Success(globalTime)), Some(Success(identity))) =>
        render(identity)(loop.stateAt(globalTime()))
      case _ => ()
    }
  }
  def futureAct: Future[Input => Unit] = actPromise.future
  
  private val actPromise = Promise[Input => Unit]()

  val loop = new StateLoop(initialState, nextState)
  val clockSync = new ClockSync(broadcast, System.currentTimeMillis)
  
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
