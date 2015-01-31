package survivor

import scala.annotation.tailrec
import World.{ unitPx, halfUnitPx }

object World {
  val width = map.head.size
  val height = map.size
  val unitPx = 32
  val halfUnitPx = unitPx/2
  val widthPx = width * unitPx
  val heightPx = height * unitPx
  def contains(x: Double, y: Double): Boolean =
    !(x < 0 || x > widthPx || y < 0 || y > heightPx)

  def initialState = State(0, Ship(32, 32, 32, 32), Ship(64, 64, 64, 64), List(), initialTurrets, initialBlocks)
  
  val initialBlocks: List[Block] = {
    map.zipWithIndex.flatMap { case (line, y) =>
      line.zipWithIndex.flatMap {
        case ('0', x) => Some(Block(row=y, col=x))
        case _ => None
      }
    }
  }
  
  val initialTurrets: List[Turret] = {
    val u = unitPx
    map.zipWithIndex.flatMap { case (line, y) =>
      line.zipWithIndex.flatMap {
        case ('┴', x) => Some(withRange(Turret(row=y, col=x, ⇧)))
        case ('┬', x) => Some(withRange(Turret(row=y, col=x, ⇩)))
        case ('┤', x) => Some(withRange(Turret(row=y, col=x, ⇦)))
        case ('├', x) => Some(withRange(Turret(row=y, col=x, ⇨)))
        case _ => None
      }
    }
  }
  
  
  val walls: List[Wall] = {
    map.zipWithIndex.flatMap { case (line, y) =>
      line.zipWithIndex.flatMap {
        case ('─', x) => Some(─(y, x))
        case ('┴', x) => Some(─(y, x))
        case ('┬', x) => Some(─(y, x))
        case ('│', x) => Some(│(y, x))
        case ('┤', x) => Some(│(y, x))
        case ('├', x) => Some(│(y, x))
        case ('┌', x) => Some(┌(y, x))
        case ('┐', x) => Some(┐(y, x))
        case ('┘', x) => Some(┘(y, x))
        case ('└', x) => Some(└(y, x))
        case _ => None
      }
    }
  }
  
  @tailrec
  private def withRange(t: Turret, range: Int = 0): Turret = {
    val nextRange = range + 1
    t.orientation match {
      case ⇧ if map(t.row - nextRange).charAt(t.col) == ' ' => withRange(t, nextRange)
      case ⇩ if map(t.row + nextRange).charAt(t.col) == ' ' => withRange(t, nextRange)
      case ⇦ if map(t.row).charAt(t.col - nextRange) == ' ' => withRange(t, nextRange)
      case ⇨ if map(t.row).charAt(t.col + nextRange) == ' ' => withRange(t, nextRange)
      case _ => t.copy(range=range)
    }
  }
  
  lazy val map: List[String] = """
    |                                                                         
    |                                                                         
    |    000000000000000000000000000                                          
    |    000000000000000000000000000                                          
    |    00                       00                                          
    |    00                       00                                          
    |    00              ┌┴─┐     00            000000000000000000            
    |    00         ┌┴──┴┘┌─┘     00           00000000000000000000           
    |    00   ┌┴┐   │     ├       00          00                  00          
    |    00   └┐└┴─┴┘     └─┐     00         00                    00         
    |    00    ┤┌────┐    ┌─┘     00        00       ┌┴┐            00        
    |    00    ││    ┤    ├       00       00        └┐└─┴─┴┐        00       
    |    00    ┤└┐┌──┘    └─┐     0000000000          ┤┌┬┐  ├         00      
    |    00    └─┘│ ┌─┬─┬┐┌┬┘     0000000000         ┌┘│ │┌┬┘         00      
    |    00       └┬┘    └┘       00       00        └┬┘ └┘          00       
    |    00                       00        00                      00        
    |    00                       00         00                    00         
    |    000000000000000000000000000          00                  00          
    |    000000000000000000000000000           00000000000000000000           
    |          00                               000000000000000000            
    |          00                                            00               
    |          00                                            00               
    |          00                                            00               
    |          00                                            00               
    |          00                               0            000              
    |          00                              000           0000             
    |          00                             00 00         00  00            
    |         000                            00   00       00    00           
    |        00 00                          00     00     00      00          
    |       00   00                        00       00   00        00         
    |      00     00                      00         00000          00        
    |     00 ┌┐ ┌┐ 00                    00  ┌┴─┴┐┌┴┐ 000  ┌┐        00       
    |    00 ┌┘└┴┘└┐ 00                  00   ┤ ┌─┘└┐└┐   ┌┴┘├         00      
    |   00  └┐   ┌┘  00                00    └┐├   └┐└┴─┴┘┌─┘          00     
    |  00    ┤   ├    00000           00      ┤│    ┤     ├             00    
    |  00   ┌┘   └┐     000000000000000       │├    │     └──┐           00   
    |   00  ┤┌┬─┬┐├  000000000000000000       ┤└────┘        ├            00  
    |    00 └┘   └┘ 00                00    ┌─┘ ┌┬┐       ┌──┘           00   
    |     00       00                  00   └┐  │ │       ├             00    
    |      00     00                    00   ┤┌─┘ │ ┌┬─┬┐ └─┐          00     
    |       00   00                      00  └┘   └┬┘   └─┬┐├         00      
    |        00 00                        00           000 └┘        00       
    |         000                          00         00000         00        
    |          0                            00   0   00   00   0   00         
    |                                        00 000 00     00 000 00          
    |                                         000 000       000 000           
    |                                          0   0         0   0            
    |                                                                         
    |                                                                         
    |                                                                         
    """.trim.stripMargin.lines.toList
}

sealed abstract class Wall(row: Int, col: Int) extends MultiHitbox {
  val x = unitPx * col.toDouble + halfUnitPx
  val y = unitPx * row.toDouble + halfUnitPx
  val cells = List(Point(x=col, y=row))
}
case class ─(row: Int, col: Int) extends Wall(row, col) {
  val hitbox1 = Hitbox(x=x, y=y+2, halfWidth=halfUnitPx, halfHeight=2)
  val hitbox2 = hitbox1
}
case class │(row: Int, col: Int) extends Wall(row, col) {
  val hitbox1 = Hitbox(x=x+2, y=y, halfWidth=2, halfHeight=halfUnitPx)
  val hitbox2 = hitbox1
}
case class ┌(row: Int, col: Int) extends Wall(row, col) {
  val hitbox1 = Hitbox(x=x+unitPx/4, y=y+2, halfWidth=unitPx/4, halfHeight=2)
  val hitbox2 = Hitbox(x=x+2, y=y+unitPx/4, halfWidth=2, halfHeight=unitPx/4)
}
case class ┐(row: Int, col: Int) extends Wall(row, col) {
  val hitbox1 = Hitbox(x=x-unitPx/4, y=y+2, halfWidth=unitPx/4, halfHeight=2)
  val hitbox2 = Hitbox(x=x+2, y=y+unitPx/4, halfWidth=2, halfHeight=unitPx/4)
}
case class ┘(row: Int, col: Int) extends Wall(row, col) {
  val hitbox1 = Hitbox(x=x-unitPx/4, y=y+2, halfWidth=unitPx/4, halfHeight=2)
  val hitbox2 = Hitbox(x=x+2, y=y-unitPx/4, halfWidth=2, halfHeight=unitPx/4)
}
case class └(row: Int, col: Int) extends Wall(row, col) {
  val hitbox1 = Hitbox(x=x+unitPx/4, y=y+2, halfWidth=unitPx/4, halfHeight=2)
  val hitbox2 = Hitbox(x=x+2, y=y-unitPx/4, halfWidth=2, halfHeight=unitPx/4)
}
