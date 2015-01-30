package survivor

import scala.collection.mutable

object Collision {
  def of(shapes: List[Shape]*): List[Shape] = {
    val inCollision = mutable.Set[Shape]()

    shapes foreach { _ foreach { shape =>
      shape.cells foreach { cell =>
        val shapesInCell = PointMap.get(cell)

        shapesInCell foreach { collisionCandidate =>
          if(collisionCandidate intersects shape) {
            inCollision add shape
            inCollision add collisionCandidate
          }
        }
        
        shapesInCell += shape
      }
    }}
    
    PointMap.clear()
    
    inCollision.toList
  }
}

object PointMap {
  private val array = new Array[Array[mutable.ArrayBuffer[Shape]]](World.height + 1)

  0 until array.length foreach { i =>
    val a = new Array[mutable.ArrayBuffer[Shape]](World.width + 1)
    0 until a.length foreach { j =>
      a(j) = new mutable.ArrayBuffer[Shape](4)
    }
    array(i) = a
  }
  
  def clear(): Unit = {
    array foreach { a =>
      0 until a.length foreach { i =>
        a(i).clear()
      }
    }
  }
  
  def get(p: Point): mutable.ArrayBuffer[Shape] = array(p.y)(p.x)
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
        // In this game, two rectangles should never intersect.
        // function intersect(a, b) {
        //   return (a.left <= b.right &&
        //           b.left <= a.right &&
        //           a.top <= b.bottom &&
        //           b.top <= a.bottom)
        false

      case (c: Circle, r: Rectangle) =>
        // Source: http://goo.gl/uwtkVFn
        val xCircleD = math.abs(c.x - r.x)
        val yCircleD = math.abs(c.y - r.y)
        if(xCircleD > (r.halfWeight + c.radius)) return false
        if(yCircleD > (r.halfHeight + c.radius)) return false
        if(xCircleD <= (r.halfWeight)) return true
        if(yCircleD <= (r.halfHeight)) return true
        val cornerD = sq(xCircleD - r.halfWeight) + sq(yCircleD - r.halfHeight)
        return cornerD <= sq(c.radius)

      case (r: Rectangle, c: Circle) =>
        shape intersects this

    }
  }
}

trait Circle extends Shape {
  def radius: Double
  val cells: List[Point] = {
    List(
      Point(1,1), Point(1,-1), Point(-1,1), Point(-1,-1)
    ).map { p =>
      Point((x + p.x * radius).toInt / World.unitPx, (y + p.y * radius).toInt / World.unitPx)
    }.distinct
  }
}

trait Rectangle extends Shape {
  def halfWeight: Double
  def halfHeight: Double
  val cells: List[Point] = {
    List(
      Point(1,1), Point(1,-1), Point(-1,1), Point(-1,-1)
    ).map { p =>
      Point((x + p.x * halfWeight).toInt / World.unitPx, (y + p.y * halfHeight).toInt / World.unitPx)
    }.distinct
  }
}

trait SingleCellRectangle extends Rectangle {
  def row: Int
  def col: Int
  override val cells: List[Point] = List(Point(x=col,y=row))
}
