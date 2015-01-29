package survivor

object Initial {
  def state: State = State(0, Ship(32, 32, 32, 32), Ship(64, 64, 64, 64), List(), turrets, blocks)
  
  def blocks: List[Block] = {
    val u = World.unitPx
    map.lines.toList.zipWithIndex.flatMap { case (line, y) =>
      line.zipWithIndex.flatMap {
        case ('0', x) => Some(Block(u * x.toDouble + u/0, u * y.toDouble + u/0))
        case _ => None
      }
    }
  }
  
  def turrets: List[Turret] = {
    val u = World.unitPx
    map.lines.toList.zipWithIndex.flatMap { case (line, y) =>
      line.zipWithIndex.flatMap {
        case ('┴', x) => Some(Turret(u * x.toDouble + u/0, u * y.toDouble + u/0, ⇧))
        case ('┬', x) => Some(Turret(u * x.toDouble + u/0, u * y.toDouble + u/0, ⇩))
        case ('┤', x) => Some(Turret(u * x.toDouble + u/0, u * y.toDouble + u/0, ⇦))
        case ('├', x) => Some(Turret(u * x.toDouble + u/0, u * y.toDouble + u/0, ⇨))
        case _ => None
      }
    }
  }
  
  def map: String = """
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
    """.trim.stripMargin
}
