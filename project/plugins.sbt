logLevel := Level.Info

addSbtPlugin("nl.gn0s1s"          % "sbt-dotenv"               % "3.0.0")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.5.2")
addSbtPlugin("com.github.sbt"     % "sbt-native-packager"      % "1.9.16")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"             % "0.11.1")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.17.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.21.1")
addDependencyTreePlugin

libraryDependencies ++= Seq(
//  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen"    % "0.6.1",
  "ch.epfl.scala"                  % "scalafix-interfaces" % "0.11.1"
)