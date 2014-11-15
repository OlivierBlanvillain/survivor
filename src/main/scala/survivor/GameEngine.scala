package survivor

import scala.collection.immutable.Queue

class GameEngine(
      initialState: State,
      nextState: (State, List[Input]) => State,
      render: State => Unit) {
  
  private var eventsSoFar: List[Event] = List()
  private val cache = new Cache
  
  def loop(time: Int): Unit = {
    // val events = eventsSoFar.dropWhile(_.time > time)
    // render(computeState(events, time))

    render(computeState(eventsSoFar, time))
  }
  
  
  def receive(event: Event): Unit = {
    System.err.println(event)
    val (newer, older) = eventsSoFar.span { e =>
      e.time > event.time || (e.time == event.time && e.random > event.random)
    }
    eventsSoFar = newer ::: event :: older
  }
  
  def computeState(events: List[Event], time: Int): State = {
    val state = computeState0(events, time)
    cache.update((events, time), state)
    state
  }
  
  def computeState0(events: List[Event], time: Int): State = {
    if(time == 0) {
      initialState
    } else {
      cache.get((events, time)).getOrElse {
        val (nowEvents, prevEvents) = events.span(_.time == time)
        nextState(computeState0(prevEvents, time - 1), nowEvents.map(_.input))
      }
    }
  }
}

class Cache {
  // private val size = 100
  // private var queue: Queue[(State, List[Event], Int)] = Queue()
  var k: (List[Event], Int) = _
  var s: State = _
  
  def update(key: (List[Event], Int), state: State): Unit = {
    k = key
    s = state
  }
  
  def get(key: (List[Event], Int)): Option[State] = {
    if(k._1.eq(key._1) && k._2 == key._2) Some(s) else None
  }
  
  // def of(events: List[Event], time: Int): Option[State] =
  //   queue.find(e => e._2 == events && e._3 == time).map(_._1)

  // def save(state: State, events: List[Event], time: Int): Unit =
  //   queue = (if(queue.length < size) queue else queue.dequeue._2).enqueue((state, events, time))
}
