# Survivor

Scala/Scala.js remake of a space-based *shoot-'em-up* arcade game with multiplayer features. The game was originally released on Atari / Commodore 64, this project is based on a web browser remake by [@scottschiller](https://github.com/scottschiller/SURVIVOR), in particular the [graphical assets](client/survivor.css) that where reused directly.

Try out the web browser version of game [online](http://olivierblanvillain.github.io/survivor), open this page twice, once for each player, and start playing with arrow keys and the space bar. 

More information can be found on [this report](https://github.com/OlivierBlanvillain/master-thesis), chapter 4 is about the game.

## Under the hood

- The [scala-lag-comp](https://github.com/OlivierBlanvillain/scala-lag-comp) framework:

    - Purely functional architecture
    - Peer-to-peer mutliplayer, no game logic on the server side
    - Predictive latency compensation

- The [scalajs-transport](https://github.com/OlivierBlanvillain/scalajs-transport) library:

    - Communication via WebRTC with WebSocket fallback
    - Serialization via [µPickle](https://github.com/lihaoyi/upickle), using default picklers

- The GUI is build around [React](http://facebook.github.io/react/):

    - Using [scalajs-react](https://github.com/japgolly/scalajs-react)
    - The JavaScript version captures user inputs directly from the DOM
    - The JVM version uses [ScalaFX](http://www.scalafx.org/), and embeds a WebKit engine to run the same rendering code than the JavaScript version (everything but the GUI runs on the JVM)

## Run it locally
  
- Local Server (or edit [JsClient](client/js/src/main/scala/JsClient.scala) and [JvmClient](client/jvm/src/main/scala/JvmClient.scala) to use the online demo server)
    - clone [scala-lag-comp](https://github.com/OlivierBlanvillain/scala-lag-comp), cd into it
    - `sbt ";project server ;run"`

- JavaScript Client

    - `sbt ";project survivorJS ;fastOptJS"`
    - open `client/index.html`

- JVM Client

    - `sbt ";project survivorJVM ;run"`

## Screenshot

![screenshot.png](screenshot.png?raw=true)
