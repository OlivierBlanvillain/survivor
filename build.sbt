val commonSettings = Seq(
  organization := "org.scalajs",
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
    "org.scalajs" %%% "transport-webrtc" % "0.1-SNAPSHOT",
    "com.scalatags" %%% "scalatags" % "0.4.3-M3",
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3"))
  .jsSettings(persistLauncher in Compile := true)
  .jsSettings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transport-javascript" % "0.1-SNAPSHOT",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.7.1-SNAPSHOT",
    "org.scala-js" %%% "scalajs-dom" % "0.7.0"))
  .jvmSettings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transport-tyrus" % "0.1-SNAPSHOT",
    "org.scalafx" %% "scalafx" % "8.0.0-R4"))
  .dependsOn(lagComp)
lazy val survivorJVM = survivor.jvm
lazy val survivorJS = survivor.js

lazy val lagComp = crossProject
  .crossType(CrossType.Pure)
  .in(file("lag-comp"))
  .settings(commonSettings: _*)
  .settings(name := "lag-comp")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transport-core" % "0.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3"))
lazy val lagCompJVM = lagComp.jvm
lazy val lagCompJS = lagComp.js

lazy val server = project
  .in(file("server"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies +=
    "org.scalajs" %% "transport-netty" % "0.1-SNAPSHOT")
