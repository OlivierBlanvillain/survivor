val commonSettings = Seq(
  organization := "com.github.olivierblanvillain",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.4",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Xfuture",
    "-Xlint"
  )
)

lazy val survivor = crossProject
  .crossType(CrossType.Full)
  .in(file("client"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "com.github.olivierblanvillain" %%% "transport-webrtc" % "0.1-SNAPSHOT",
    "com.github.olivierblanvillain" %%% "scala-lag-comp" % "0.1-SNAPSHOT",
    "com.scalatags" %%% "scalatags" % "0.4.3-M3",
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3"))
  .jsSettings(persistLauncher in Compile := true)
  // .jsSettings(packageJSDependencies := true) // Broken in M3.
  .jsSettings(libraryDependencies ++= Seq(
    "com.github.olivierblanvillain" %%% "transport-javascript" % "0.1-SNAPSHOT",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.7.1-SNAPSHOT",
    "org.scala-js" %%% "scalajs-dom" % "0.7.0"))
  .jvmSettings(libraryDependencies ++= Seq(
    "com.github.olivierblanvillain" %%% "transport-tyrus" % "0.1-SNAPSHOT",
    "org.scalafx" %% "scalafx" % "8.0.0-R4"))
lazy val survivorJVM = survivor.jvm
lazy val survivorJS = survivor.js
