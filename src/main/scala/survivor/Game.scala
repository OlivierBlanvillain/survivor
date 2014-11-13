package survivor

import scala.collection.immutable.Queue

object Game {
  val history = new History()
  
  def computeState(inputs: List[Event], time: Int): State = {
    
    val state: State = if(time == 0) initialState else {
      (inputs, history of (inputs, time)) match {
        case _ => ???
      }
    }
    
    state
  }
  
  // def nextState(state: State): State = {
    
  // }
  
  val initialState: State = State(Ship(32, 32), List())
}

class History {
  private val size = 100
  private var queue: Queue[(List[Event], Int, State)] = Queue()
  
  def of(inputs: List[Event], time: Int): Option[State] =
    queue.find(e => e._1 == inputs && e._2 == time).map(_._3)

  def save(inputs: List[Event], time: Int, state: State): Unit =
    queue = (if(queue.length < size) queue else queue.dequeue._2).enqueue((inputs, time, state))
}
