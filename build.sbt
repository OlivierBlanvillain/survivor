import ScalaJSKeys._

scalaJSSettings

// workbenchSettings

name := "Survivor"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.2"

persistLauncher := true

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-encoding", "utf8"
)


skip in packageJSDependencies := false

libraryDependencies ++= Seq(
  "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
  "com.github.japgolly.scalajs-react" %%% "core" % "0.5.1"
)

jsDependencies ++= Seq (
  "org.webjars" % "react" % "0.11.1" / "react-with-addons.js" commonJSName "React"
)

// bootSnippet := "((typeof global === 'object' && global && global['Object'] === Object) ? global : this)['survivor']['Main']().main();"

// ScalaJSKeys.inliningMode := scala.scalajs.sbtplugin.InliningMode.Off

// spliceBrowsers <<= spliceBrowsers.triggeredBy(ScalaJSKeys.fastOptJS in Compile)
