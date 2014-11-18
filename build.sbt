import ScalaJSKeys._

val commonSettings = Seq(
  name := "Survivor",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.2",
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-encoding", "utf8"
  )
)

// workbenchSettings

// bootSnippet := "((typeof global === 'object' && global && global['Object'] === Object) ? global : this)['survivor']['Main']().main();"

// ScalaJSKeys.inliningMode := scala.scalajs.sbtplugin.InliningMode.Off

// spliceBrowsers <<= spliceBrowsers.triggeredBy(ScalaJSKeys.fastOptJS in Compile)

lazy val play = project.in(file("play"))
  .enablePlugins(PlayScala)
  .settings((commonSettings): _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %% "transportplay" % "0.1-SNAPSHOT",
    "org.scalajs" %% "transportakkajvm" % "0.1-SNAPSHOT"))

lazy val js = project.in(file("js"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .settings(unmanagedSourceDirectories in Compile += (baseDirectory in play).value / "../shared")
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "transportjavascript" % "0.1-SNAPSHOT",
    "org.scalajs" %%% "transportakkajs" % "0.1-SNAPSHOT",
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.5.1"))
  .settings(jsDependencies ++= Seq (
    "org.webjars" % "sockjs-client" % "0.3.4" / "sockjs.min.js" commonJSName "SockJS",
    "org.webjars" % "react" % "0.11.1" / "react-with-addons.js" commonJSName "React"))
  .settings(persistLauncher := true)
  .settings(skip in packageJSDependencies := false)
