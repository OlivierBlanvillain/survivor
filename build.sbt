import ScalaJSKeys._

val commonSettings = Seq(
  name := "Survivor",
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

// workbenchSettings

// bootSnippet := "((typeof global === 'object' && global && global['Object'] === Object) ? global : this)['survivor']['Main']().main();"

// ScalaJSKeys.inliningMode := scala.scalajs.sbtplugin.InliningMode.Off

// spliceBrowsers <<= spliceBrowsers.triggeredBy(ScalaJSKeys.fastOptJS in Compile)

lazy val server = project.in(file("server"))
  .settings((commonSettings): _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %% "transportnetty" % "0.1-SNAPSHOT"))

lazy val jsClient = project.in(file("js-client"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transportjavascript" % "0.1-SNAPSHOT",
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
    "com.lihaoyi" %%% "upickle" % "0.2.5",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.5.1"))
  .settings(jsDependencies ++= Seq (
    "org.webjars" % "sockjs-client" % "0.3.4" / "sockjs.min.js",
    "org.webjars" % "react" % "0.11.1" / "react-with-addons.js" commonJSName "React"))
  .settings(persistLauncher := true)
  .settings(skip in packageJSDependencies := false)
