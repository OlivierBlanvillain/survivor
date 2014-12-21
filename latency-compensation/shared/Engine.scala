package lagcomp

import scala.concurrent._
import transport.ConnectionHandle

case class Event[Input](input: Input, peer: Peer, time: Int)

sealed trait Peer
case object P1 extends Peer
case object P2 extends Peer

class Engine[Input, State](
      initialState: State,
      nextState: (State, Set[Event[Input]]) => State,
      render: State => Unit,
      connection: ConnectionHandle
    )(implicit
      ec: ExecutionContext,
      iw: upickle.Writer[Event[Input]],
      ir: upickle.Reader[Event[Input]]
    ) {
  
  type Act = Input => Unit
  type Render = () => Unit

  private val actPromise = Promise[Act]()
  private val renderPromise = Promise[Render]()

  def futureAct: Future[Act] = actPromise.future
  def futureRender: Future[Render] = renderPromise.future
  
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
    val event = Event(input, identity, globalTime())
    loop.receive(event)
    connection.write(upickle.write(event))
  }
}
