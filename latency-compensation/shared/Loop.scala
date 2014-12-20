package lagcomp

class Loop[Input, State](
      initialState: State,
      nextState: (State, List[Event[Input]]) => State,
      render: State => Unit) {
  
  private var eventsSoFar: List[Event[Input]] = List()
  private val cache = new WeakMap[List[Event[Input]], State](100)
  
  def render(time: Int): Unit = {
    // If one clock is ahead of the other we might receive inputs from the future.
    val events = eventsSoFar.dropWhile(_.time > time)
    render(computeState(time, events))
  }
  
  def receive(event: Event[Input]): Unit = {
    // TODO: time break the == cases with something?
    val (newer, older) = eventsSoFar.span(_.time > event.time)
    eventsSoFar = newer ::: event :: older
  }
  
  def computeState(time: Int, events: List[Event[Input]]): State = {
    lazy val (nowEvents, prevEvents) = events.span(_.time == time)
    lazy val recursively = nextState(computeState(time - 1, prevEvents), nowEvents)
    if(time == 0) {
      initialState
    } else {
      cache.getOrElseUpdate(time, events, recursively)
    }
  }
}

