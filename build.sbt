scalaJSSettings

name := "Survivor"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.2"

ScalaJSKeys.persistLauncher := true

libraryDependencies ++= Seq(
  "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"
)
