package survivor

import scala.collection.mutable

object Collision {
  def of(shapes: List[Shape]): List[Shape] = {
    val inCollision = mutable.Set[Shape]()
    val map = mutable.Map[Point, mutable.Set[Shape]]()

    shapes foreach { shape =>
      shape.cells foreach { cell =>
        val shapesInCell = map.getOrElseUpdate(cell, mutable.Set[Shape]())

        shapesInCell foreach { collisionCandidate =>
          if(collisionCandidate intersects shape) {
            inCollision add shape
            inCollision add collisionCandidate
          }
        }
        
        shapesInCell add shape
      }
    }

    inCollision.toList
  }
}

case class Point(x: Int, y: Int)

trait Shape {
  def cells: List[Point]
  
  def x: Double
  def y: Double
  
  def intersects(shape: Shape): Boolean = { 
    def sq(d: Double) = d * d
    
    (this, shape) match {
      
      case (c1: Circle, c2: Circle) =>
        val distSq = sq(c1.x - c2.x) + sq(c1.y - c2.y)
        sq(c1.radius - c2.radius) <= distSq && distSq <= sq(c1.radius + c2.radius)
        
      case (r1: Rectangle, r2: Rectangle) =>
        // With the currents objects, two rectangles should never be in the same cell.
        // function intersect(a, b) {
        //   return (a.left <= b.right &&
        //           b.left <= a.right &&
        //           a.top <= b.bottom &&
        //           b.top <= a.bottom)
        ???

      case (c: Circle, r: Rectangle) =>
        // Source: http://goo.gl/uwtkVFn
        val xCircleD = math.abs(c.x - r.x)
        val yCircleD = math.abs(c.y - r.y)
        lazy val cornerD = sq(xCircleD - r.halfWeight) + sq(yCircleD - r.halfHeight)
        if(xCircleD > (r.halfWeight + c.radius)) return false
        if(yCircleD > (r.halfHeight + c.radius)) return false
        if(xCircleD <= (r.halfWeight)) return true
        if(yCircleD <= (r.halfHeight)) return true
        return cornerD <= sq(c.radius)

      case (r: Rectangle, c: Circle) =>
        shape intersects this

    }
  }
}

trait Circle extends Shape {
  def cells: List[Point] = {
    List(
      Point(1,1), Point(1,-1), Point(-1,1), Point(-1,-1)
    ).map { p =>
      Point((x + p.x * radius).toInt / World.unitPx, (y + p.y * radius).toInt / World.unitPx)
    }.distinct
  }
  
  def radius: Double
}

trait Rectangle extends Shape {
  def cells: List[Point] = {
    List(
      Point(1,1), Point(1,-1), Point(-1,1), Point(-1,-1)
    ).map { p =>
      Point((x + p.x * halfWeight).toInt / World.unitPx, (y + p.y * halfHeight).toInt / World.unitPx)
    }.distinct
  }
  
  def halfWeight: Double
  def halfHeight: Double
}
