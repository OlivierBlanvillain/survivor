package survivor

import scala.collection.immutable.Queue

class Engine(
      initialState: State,
      nextState: (State, List[Input]) => State,
      render: State => Unit) {
  
  private var eventsSoFar: List[Event] = List()
  private val cache = new WeakMap
  
  def loop(time: Int): Unit = {
    // If one clock is ahead of the other we might receive inputs from the future.
    val events = eventsSoFar.dropWhile(_.time > time)
    render(computeState(time, events))
  }
  
  def receive(event: Event): Unit = {
    // TODO: time break the == cases with something.
    val (newer, older) = eventsSoFar.span(_.time > event.time)
    eventsSoFar = newer ::: event :: older
  }
  
  def computeState(time: Int, events: List[Event]): State = {
    lazy val (nowEvents, prevEvents) = events.span(_.time == time)
    lazy val recursively = nextState(computeState(time - 1, prevEvents), nowEvents.map(_.input))
    if(time == 0) {
      initialState
    } else {
      cache.getOrElseUpdate(time, events, recursively)
    }
  }
}

class WeakMap {
  val size = 100
  var array = new Array[(Int, List[Event], State)](size)
  
  def getOrElseUpdate(time: Int, events: List[Event], state: => State): State = {
    val index = time % size
    val value = array(index)
    if(value != null && value._1 == time && value._2 == events) {
      value._3
    } else {
      array(index) = (time, events, state)
      state
    }
  }
}
