package lagcomp

import scala.concurrent.Future
import transport.ConnectionHandle

case class Event[Input](input: Input, peer: Peer, time: Int)

sealed trait Peer
case object P1 extends Peer
case object P2 extends Peer

class Engine[Input, State](
    initialState: State,
    nextState: (State, List[Event[Input]]) => State,
    render: State => Unit,
    connection: ConnectionHandle) {
  
  def now(): Int = ???
  val localPeer: Peer = ???
  
  val loop = new Loop(initialState, nextState, render)
    
  def act(input: Input): Unit = {
    val event = Event(input, localPeer, now())
    loop receive event
  }
  def render(): Unit = loop.render(now())
}
