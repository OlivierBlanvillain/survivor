package survivor

import scala.collection.immutable.Queue

class Engine(
      initialState: State,
      nextState: (State, List[Input]) => State,
      render: State => Unit) {
  
  private var eventsSoFar: List[Event] = List()
  private val cache = new Cache
  
  def loop(time: Int): Unit = {
    // If one clock is ahead of the other we might receive inputs from the future.
    val events = eventsSoFar.dropWhile(_.time > time)
    render(computeState(events, time))
  }
  
  def receive(event: Event): Unit = {
    // TODO: time break the == cases with something.
    val (newer, older) = eventsSoFar.span(_.time > event.time)
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
  var k: (List[Event], Int) = _
  var s: State = _
  
  def update(key: (List[Event], Int), state: State): Unit = {
    k = key
    s = state
  }
  
  /** Optionally returns the value associated with a key. */
  def get(key: (List[Event], Int)): Option[State] = {
    // if(k._1.eq(key._1) && k._2 == key._2) {
    //   System.err.println("!! HIT !! " + key._2)
    // } else {
    //   System.err.println("Miss, LF " + key._2 + " " + key._1)
    //   System.err.println("Miss, Got " + k._2 + " " + k._1)
    // }
    if(k._1.eq(key._1) && k._2 == key._2) Some(s) else None
  }
  
  /** Adds a new key/value pair to this map. */
  // def update(key: A, value: B): Unit = ???
  
  /** If given key is already in this map, returns associated value. Otherwise, computes
    * value from given expression `op`, stores with key in map and returns that value.
  // def getOrElseUpdate(key: A, op: => B): B =
  //   get(key) match {
  //     case Some(v) => v
  //     case None => val d = op; this(key) = d; d
  //   }
}
