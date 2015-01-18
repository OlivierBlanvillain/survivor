package lagcomp

class WeakMap[K, V](size: Int) {
  val array = new Array[(Int, K, V)](size)
  
  def getOrElseUpdate(time: Int, key: K, value: => V): V = {
    if(time < 0) throw new IndexOutOfBoundsException(time.toString)
    val index = time % size
    val element = array(index)
    if(element != null && element._1 == time && element._2 == key) {
      element._3
    } else {
      array(index) = (time, key, value)
      value
    }
  }
  
  def get(time: Int, key: K): Option[V] = None
}
