import sbt.{file, project}

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.5.0"
ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature"
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val zioVersion        = "2.1.5"
val tapirVersion      = "1.10.7"
val zioLoggingVersion = "2.3.0"
val zioConfigVersion  = "4.0.2"
val sttpVersion       = "3.9.6"
val javaMailVersion   = "1.6.2"
val stripeVersion     = "24.3.0"

val dependencies = Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"                 % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"                    % tapirVersion,
  "com.softwaremill.sttp.client3" %% "zio"                               % sttpVersion,
  "dev.zio"                       %% "zio-json"                          % "0.7.0",
  "com.softwaremill.sttp.tapir"   %% "tapir-zio"                         % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"             % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"           % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"            % tapirVersion % "test",
  "dev.zio"                       %% "zio-logging"                       % zioLoggingVersion,
  "dev.zio"                       %% "zio-logging-slf4j"                 % zioLoggingVersion,
  "ch.qos.logback"                 % "logback-classic"                   % "1.5.6",
  "dev.zio"                       %% "zio-test"                          % zioVersion,
  "dev.zio"                       %% "zio-test-junit"                    % zioVersion   % "test",
  "dev.zio"                       %% "zio-test-sbt"                      % zioVersion   % "test",
  "dev.zio"                       %% "zio-test-magnolia"                 % zioVersion   % "test",
  "dev.zio"                       %% "zio-mock"                          % "1.0.0-RC9"  % "test",
  "dev.zio"                       %% "zio-config"                        % zioConfigVersion,
  "dev.zio"                       %% "zio-config-magnolia"               % zioConfigVersion,
  "dev.zio"                       %% "zio-config-typesafe"               % zioConfigVersion,
  "io.getquill"                   %% "quill-jdbc-zio"                    % "4.8.4",
  "org.postgresql"                 % "postgresql"                        % "42.7.3",
  "org.flywaydb"                   % "flyway-core"                       % "9.7.0",
  "io.github.scottweaver"         %% "zio-2-0-testcontainers-postgresql" % "0.10.0",
  "dev.zio"                       %% "zio-prelude"                       % "1.0.0-RC16",
  "com.auth0"                      % "java-jwt"                          % "4.4.0",
  "com.sun.mail"                   % "javax.mail"                        % javaMailVersion,
  "com.stripe"                     % "stripe-java"                       % stripeVersion
)

val Cctt: String = "compile->compile;test->test"

lazy val foundations = (project in file("modules/foundations"))
  .settings(
    libraryDependencies ++= dependencies
  )

//lazy val db = (project in file("modules/db"))
//  .settings(
//    libraryDependencies ++= dependencies
//  )

lazy val server = (project in file("modules/server"))
  .settings(
    libraryDependencies ++= dependencies
  )

lazy val integration = (project in file("modules/integration"))
  .settings(
//    name := "integration",
//    settings,
    libraryDependencies ++= dependencies,
    Test / fork    := true,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(server % Cctt)


lazy val root = (project in file("."))
  .settings(
    name := "zrop"
  )
  .aggregate(foundations, server, integration)
  .dependsOn(foundations, server, integration)