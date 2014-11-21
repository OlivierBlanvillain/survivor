package survivor

import org.scalajs.dom

class KeyboardListener(fireEvent: Event => Unit, getFrame: () => Int, me: Player) {
  
  var lastKey: (Key, Action) = (Space, Release)
  
  dom.window.addEventListener("keydown", listener(Press) _, false)
  dom.window.addEventListener("keyup", listener(Release) _, false)

  def listener(action: Action)(e: dom.Event): Unit = {
    val domEvent = e.asInstanceOf[dom.KeyboardEvent]
    val optionalKey = domEvent.keyCode match {
      case 32 => Some(Space)
      case 37 => Some(Left)
      case 38 => Some(Up)
      case 39 => Some(Right)
      case 40 => Some(Down)
      case _ => None
    }
    optionalKey.foreach { key =>
      domEvent.preventDefault()
      if((key, action) != lastKey) {
        lastKey = (key, action)
        fireEvent(Event(Input(key, action, me), getFrame() + 1))
      }
    }
  }
}
