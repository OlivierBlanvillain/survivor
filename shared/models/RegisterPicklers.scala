package models

import org.scalajs.spickling._

object RegisterPicklers {
  import PicklerRegistry.register

  register[Msg]
  register[Connected]
  
  register(Up)
  register(Down)
  register(Right)
  register(Left)
  register(Space)
  register(Press)
  register(Release)
  register(Me)
  register(Him)
  register(⇦)
  register(⇨)
  register(⬄)
  register(⇧)
  register(⇩)
  register(⇳)
  register[Input]
  register[Event]
  
  def registerPicklers(): Unit = ()
}
