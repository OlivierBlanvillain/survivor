import ScalaJSKeys._

val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.2",
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
    "org.scalajs" %% "transportnetty" % "0.1-SNAPSHOT"))

lazy val jsClient = project.in(file("js-client"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(persistLauncher := true)
  .settings(skip in packageJSDependencies := false)
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
    "org.scalajs" %%% "transportjavascript" % "0.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.5"))
  .settings(jsDependencies +=
    "org.webjars" % "sockjs-client" % "0.3.4" / "sockjs.min.js")
  .dependsOn(reactUi)
  .dependsOn(jsLagComp)

lazy val jvmClient = project.in(file("jvm-client"))
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalafx" %% "scalafx" % "8.0.0-R4",
    "org.scalajs" %% "transporttyrus" % "0.1-SNAPSHOT",
    "com.scalatags" %% "scalatags" % "0.4.2",
    "com.lihaoyi" %% "upickle" % "0.2.5"))
  .dependsOn(jvmLagComp)

lazy val reactUi = project.in(file("react-ui"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(skip in packageJSDependencies := false)
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
    "org.scalajs" %%% "transportjavascript" % "0.1-SNAPSHOT",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.5.1",
    "com.lihaoyi" %%% "upickle" % "0.2.5"))
  .settings(jsDependencies +=
    "org.webjars" % "react" % "0.11.1" / "react-with-addons.js" commonJSName "React")

lazy val jsLagComp = project.in(file("latency-compensation/js"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transportjavascript" % "0.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.5"))

lazy val jvmLagComp = project.in(file("latency-compensation/jvm"))
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %% "transporttyrus" % "0.1-SNAPSHOT",
    "com.lihaoyi" %%% "upickle" % "0.2.5"))
