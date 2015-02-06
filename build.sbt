val commonSettings = Seq(
  organization := "com.github.olivierblanvillain",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.5",
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
    "com.lihaoyi" %%% "scalatags" % "0.4.5"))
  .jsSettings(persistLauncher in Compile := true)
  .jsSettings(skip in packageJSDependencies := false)
  .jsSettings(libraryDependencies ++= Seq(
    "com.github.olivierblanvillain" %%% "transport-javascript" % "0.1-SNAPSHOT",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.7.3-SNAPSHOT",
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"))
  .jsSettings(jsDependencies +=
    "org.webjars" % "react" % "0.12.1" / "react-with-addons.min.js" commonJSName "React")
  .jvmSettings(libraryDependencies ++= Seq(
    "com.github.olivierblanvillain" %%% "transport-tyrus" % "0.1-SNAPSHOT",
    "org.scalafx" %% "scalafx" % "8.0.0-R4"))

lazy val survivorJVM = survivor.jvm
lazy val survivorJS = survivor.js
