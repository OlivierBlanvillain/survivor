package survivor

import scala.collection.immutable.Queue

class GameEngine(
      initialState: State,
      nextState: (State, List[Input]) => State,
      render: State => Unit) {
  
  private var events: List[Event] = List()
  private val cache = new Cache()
  
  def loop(time: Int): Unit = {
    val state = computeState(events, time)
    cache.save(state, events, time)
    render(state)
  }
  
  
  def receive(event: Event): Unit ={
    print(event)
    // TODO: properly insert ooo events
    events = event :: events
  }
  
  def computeState(events: List[Event], time: Int): State = {
    if(time == 0) {
      initialState
    } else {
      val (nowEvents, prevEvents) = events.span(_.time == time)
      lazy val recursively = computeState(prevEvents, time - 1)
      cache.of(events, time) getOrElse nextState(recursively, nowEvents.map(_.input))
    }
  }
}

class Cache {
  private val size = 100
  private var queue: Queue[(State, List[Event], Int)] = Queue()
  
  def of(events: List[Event], time: Int): Option[State] =
    queue.find(e => e._2 == events && e._3 == time).map(_._1)

  def save(state: State, events: List[Event], time: Int): Unit =
    queue = (if(queue.length < size) queue else queue.dequeue._2).enqueue((state, events, time))
}
