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

lazy val server = project.in(file("server"))
  .settings((commonSettings): _*)
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %% "transportnettyjvm" % "0.1-SNAPSHOT"))

lazy val jsClient = project.in(file("js-client"))
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(persistLauncher := true)
  // .settings(skip in packageJSDependencies := false)
  .settings(libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.7.0",
    "org.scalajs" %%% "transportjavascriptjs" % "0.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3"))
  // .settings(jsDependencies +=
  //   "org.webjars" % "sockjs-client" % "0.3.4" / "sockjs.min.js")
  .dependsOn(reactUi)
  .dependsOn(jsLagComp)

lazy val jvmClient = project.in(file("jvm-client"))
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalafx" %% "scalafx" % "8.0.0-R4",
    "org.scalajs" %% "transporttyrusjvm" % "0.1-SNAPSHOT",
    "com.scalatags" %% "scalatags" % "0.4.3-M3",
    "com.lihaoyi" %% "upickle" % "0.2.6-M3"))
  .dependsOn(jvmLagComp)

lazy val reactUi = project.in(file("react-ui"))
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  // .settings(skip in packageJSDependencies := false)
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transportjavascriptjs" % "0.1-SNAPSHOT",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.7.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3"))
  // .settings(jsDependencies +=
  //   "org.webjars" % "react" % "0.12.1" / "react-with-addons.js" commonJSName "React")
  .dependsOn(jsLagComp)

lazy val jsLagComp = project.in(file("latency-compensation/js"))
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transportjavascriptjs" % "0.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3"))

lazy val jvmLagComp = project.in(file("latency-compensation/jvm"))
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %% "transporttyrusjvm" % "0.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3"))
