package survivor

import scala.scalajs.js
import org.scalajs.dom
import js.annotation.JSExport

object Main extends js.JSApp {
  
  def main(): Unit = {
    println("SUP JS?")
  }
  
}

@JSExport
object CssUtils {
  // @JSExport
  // def has(e: js.Dynamic, c: String): Boolean =
  //   true
  //   // e.className.asInstanceOf[String].contains(c)

  @JSExport
  def add(e: js.Dynamic, c: String): Unit =
    if(e != null)
      e.className = e.className + " " + c

  @JSExport
  def remove(e: js.Dynamic, c: String): Unit =
    if(e != null)
    e.className = e.className.replace(c, "")

  @JSExport
  def toggle(e: js.Dynamic, c: String): Unit =
    if(e != null) {
    remove(e, c)
    add(e, c)
  }

  @JSExport
  def swap(e: js.Dynamic, c1: String, c2: String): Unit =
    if(e != null) {
    remove(e, c1)
    add(e, c2)
  }
}

@JSExport
object EventUtils {
  @JSExport
  def add(e: dom.Element, name: String, handler: js.Function1[dom.Event, _]): Unit =
    if(e != null)
    e.addEventListener(name, handler)

  @JSExport
  def remove(e: dom.Element, name: String, handler: js.Function1[dom.Event, _]): Unit =
    if(e != null)
    e.removeEventListener(name, handler)

  @JSExport
  def preventDefault(e: dom.Event) =
    if(e != null)
    e.preventDefault()
}
