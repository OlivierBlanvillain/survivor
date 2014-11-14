package survivor

import scala.collection.immutable.Queue

class GameEngine(
      initialState: State,
      nextState: (State, List[Input]) => State,
      render: State => Unit) {
  
  private var events: List[Event] = List()
  private val cache = new scala.collection.mutable.HashMap[(List[Event], Int), State]()
  
  def loop(time: Int): Unit = {
    render(computeState(events, time))
  }
  
  
  def receive(event: Event): Unit ={
    System.err.println(event)
    val (newer, older) = events.span { e =>
      e.time < event.time || (e.time == event.time && e.random < event.random)
    }
    events = newer ::: event :: older
  }
  
  def computeState(events: List[Event], time: Int): State = {
    if(time == 0) {
      initialState
    } else {
      val (nowEvents, prevEvents) = events.span(_.time == time)
      def recursively = nextState(computeState(prevEvents, time - 1), nowEvents.map(_.input))
      cache.getOrElseUpdate((events, time), recursively)
    }
  }
}

class Cache {
  private val size = 100
  private var queue: Queue[(State, List[Event], Int)] = Queue()

  def getOrElseUpdate(key: (List[Event], Int), op: ⇒ State): State = ???

  // def of(events: List[Event], time: Int): Option[State] =
  //   queue.find(e => e._2 == events && e._3 == time).map(_._1)

  // def save(state: State, events: List[Event], time: Int): Unit =
  //   queue = (if(queue.length < size) queue else queue.dequeue._2).enqueue((state, events, time))
}
