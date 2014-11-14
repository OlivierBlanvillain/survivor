package survivor

sealed trait Key
case object Up extends Key
case object Down extends Key
case object Right extends Key
case object Left extends Key
case object Shift extends Key

sealed trait Player
case object Me extends Player
case object He extends Player

case class Input(key: Key, pressed: Boolean, player: Player)

case class Event(input: Input, time: Int)
