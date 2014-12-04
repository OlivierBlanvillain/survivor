package survivor

import scala.collection.mutable

object Collision {
  def of(shapes: List[Shape], time: Int): List[Shape] = {
    val inCollision = mutable.Set[Shape]()
    val map = mutable.Map[Point, mutable.Set[Shape]]()

    shapes.foreach { shape =>
      shape.cells(time).foreach { cell =>
        val shapesInCell = map.getOrElseUpdate(cell, mutable.Set[Shape]())

        shapesInCell.foreach { collisionCandidate =>
          if(collisionCandidate.intersects(shape, time)) {
            inCollision.add(shape)
            inCollision.add(collisionCandidate)
          }
        }
        
        shapesInCell.add(shape)
      }
    }

    inCollision.toList
  }
}

case class Point(x: Int, y: Int)

trait Shape {
  def cells(time: Int): List[Point]
  
  def x(time: Int): Double
  def y(time: Int): Double
  
  def intersects(shape: Shape, t: Int): Boolean = { 
    def sq(d: Double) = d * d
    
    (this, shape) match {
      
      case (c1: Circle, c2: Circle) =>
        val distSq = sq(c1.x(t) - c2.x(t)) + sq(c1.y(t) - c2.y(t))
        sq(c1.radius - c2.radius) <= distSq && distSq <= sq(c1.radius + c2.radius)
        
      case (r1: Rectangle, r2: Rectangle) =>
         // function intersect(a, b) {
         //   return (a.left <= b.right &&
         //           b.left <= a.right &&
         //           a.top <= b.bottom &&
         //           b.top <= a.bottom)
        ???

      case (c: Circle, r: Rectangle) =>
        // Source: http://goo.gl/uwtkVFn
        val xCircleD = math.abs(c.x(t) - r.x(t))
        val yCircleD = math.abs(c.y(t) - r.y(t))
        lazy val cornerD = sq(xCircleD - r.halfWeight(t)) + sq(yCircleD - r.halfHeight(t))
        if (xCircleD > (r.halfWeight(t) + c.radius)) return false
        if (yCircleD > (r.halfHeight(t) + c.radius)) return false
        if (xCircleD <= (r.halfWeight(t))) return true
        if (yCircleD <= (r.halfHeight(t))) return true
        return (cornerD <=  sq(c.radius))

      case (r: Rectangle, c: Circle) =>
        shape.intersects(this, t)

    }
  }
}

trait Circle extends Shape {
  def cells(t: Int): List[Point] = List(
    Point(1,1), Point(1,-1), Point(-1,1), Point(-1,-1)
  ).map { p =>
    Point((radius + p.x * x(t)).toInt / World.unitPx, (radius + p.y * y(t)).toInt / World.unitPx)
  }.distinct
  
  def radius: Double
}

trait Rectangle extends Shape {
  def halfWeight(time: Int): Double
  def halfHeight(time: Int): Double
}
