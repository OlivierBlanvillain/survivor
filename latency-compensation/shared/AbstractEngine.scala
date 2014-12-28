package lagcomp

import scala.concurrent._
import transport.ConnectionHandle

case class Action[Input](input: Input, peer: Peer)

abstract
class AbstractEngine[Input, State](
      initialState: State,
      nextState: (State, Set[Action[Input]]) => State,
      render: State => Unit,
      broadcast: ConnectionHandle){

  def triggerRendering(): Unit
  def futureAct: Future[Input => Unit]
}
