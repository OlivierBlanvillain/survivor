package survivor

sealed trait Key
case object Up extends Key
case object Down extends Key
case object Right extends Key
case object Left extends Key
case object Space extends Key

sealed trait Action
case object Press extends Action
case object Release extends Action

sealed trait Player
case object Me extends Player
case object He extends Player

sealed trait XOr
case object ⇦ extends XOr
case object ⇨ extends XOr
case object ⬄ extends XOr

sealed trait YOr
case object ⇧ extends YOr
case object ⇩ extends YOr
case object ⇳ extends YOr

case class Input(key: Key, action: Action)

case class Event(input: Input, time: Int, player: Player)
