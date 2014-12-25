package lagcomp

import scala.concurrent._
import transport.ConnectionHandle
import scala.util.Success

case class Move[Input](input: Input, peer: Peer)
case class Event[Input](move: Move[Input], time: Int)

sealed trait Peer
case object P1 extends Peer
case object P2 extends Peer

class Engine[Input, State](
      initialState: State,
      nextState: (State, Set[Move[Input]]) => State,
      render: State => Unit,
      connection: ConnectionHandle
    )(implicit
      ec: ExecutionContext,
      iw: upickle.Writer[Event[Input]],
      ir: upickle.Reader[Event[Input]]
    ) {
  
  type Act = Input => Unit
  type Render = () => Unit

  def triggerRendering(): Unit = {
    clockSync.futureGlobalTime.value match {
      case Some(Success(globalTime)) =>
        loop.render(globalTime())
      case _ =>
        loop.render(0)
    }
  }
  def futureAct: Future[Act] = actPromise.future
  
  private val actPromise = Promise[Act]()
  private val renderPromise = Promise[Render]()

  val loop = new Loop(initialState, nextState, render)     
  val clockSync = new ClockSync(connection)
  
  connection.handlerPromise.success { pickle =>
    if(clockSync.pending)
      clockSync.receive(pickle)
    else
      loop.receive(upickle.read[Event[Input]](pickle))
  }
  
  for {
    globalTime <- clockSync.futureGlobalTime
  } renderPromise.success(() => loop.render(globalTime()))
  
  for {
    globalTime <- clockSync.futureGlobalTime
    identity <- clockSync.futureIdentity
  } actPromise.success { input =>
    val event = Event(Move(input, identity), globalTime())
    loop.receive(event)
    connection.write(upickle.write(event))
  }
}
