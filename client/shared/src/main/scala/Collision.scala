package survivor

import scala.collection.mutable

object Collision {
  def of(shapes: List[Shape]*): List[Shape] = {
    val inCollision = mutable.Set[Shape]()

    for(shapeList <- shapes; shape <- shapeList; cell <- shape.cells) {
      val shapesInCell = pointMap(cell.y)(cell.x)
      shapesInCell foreach { collisionCandidate =>
        if(collisionCandidate intersects shape) {
          inCollision add shape
          inCollision add collisionCandidate
        }
      }
      shapesInCell += shape
    }
    
    for(xs <- pointMap; x <- xs) x.clear()
    
    inCollision.toList
  }
  
  val pointMap = Array.tabulate[mutable.ArrayBuffer[Shape]](
    World.height + 1, World.width + 1)((_, _) => new mutable.ArrayBuffer[Shape](4))
}

case class Point(x: Int, y: Int)

sealed trait Shape {
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
        // In this game, two rectangles should never intersect!
        // a.left <= b.right && b.left <= a.right && a.top <= b.bottom && b.top <= a.bottom
        false

      case (c: Circle, r: Rectangle) =>
        // Source: http://goo.gl/uwtkVFn
        val xCircleD = math.abs(c.x - r.x)
        val yCircleD = math.abs(c.y - r.y)
        if(xCircleD > (r.halfWidth + c.radius)) return false
        if(yCircleD > (r.halfHeight + c.radius)) return false
        if(xCircleD <= (r.halfWidth)) return true
        if(yCircleD <= (r.halfHeight)) return true
        val cornerD = sq(xCircleD - r.halfWidth) + sq(yCircleD - r.halfHeight)
        return cornerD <= sq(c.radius)

      case (r: Rectangle, c: Circle) =>
        shape intersects this
        
      case (m: MultiHitbox, _) =>
        m.hitbox1.intersects(shape) || m.hitbox2.intersects(shape) 
        
      case (_, m: MultiHitbox) =>
        shape intersects this
    }
  }
}

trait Circle extends Shape {
  def radius: Double
  def cells: List[Point] = {
    List(
      Point(1,1), Point(1,-1), Point(-1,1), Point(-1,-1)
    ).map { p =>
      Point((x + p.x * radius).toInt / World.unitPx, (y + p.y * radius).toInt / World.unitPx)
    }.distinct
  }
}

trait Rectangle extends Shape {
  def halfWidth: Int
  def halfHeight: Int
}

trait MultiHitbox extends Shape {
  val hitbox1: Shape
  val hitbox2: Shape
}

case class Hitbox(x: Double, y: Double, halfWidth: Int, halfHeight: Int) extends Rectangle {
  val cells = Nil 
}
