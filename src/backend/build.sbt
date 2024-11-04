name := "HiitBeats"

version := "0.1"

scalaVersion := "2.13.12"

lazy val example = project.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
	  "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M2", 
	  "com.typesafe.play" %% "play-json" % "2.10.1",
	  "org.http4s" %% "http4s-dsl" % "0.23.18",
	  "org.http4s" %% "http4s-ember-server" % "0.23.18",
	  "org.scala-lang" % "scala-library" % "2.13.12",
	  "org.typelevel" %% "cats-effect" % "3.5.1",
      "com.comcast" %% "ip4s-core" % "3.2.0",
      "com.typesafe.akka" %% "akka-actor-typed"     % "2.6.20",
      "com.typesafe.akka" %% "akka-http"            % "10.2.10",
      "com.typesafe.akka" %% "akka-stream"          % "2.6.20",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
      "io.spray"          %% "spray-json"           % "1.3.6",
      "org.scalatest" %% "scalatest" % "3.2.16" % Test 
    ),
    dependencyOverrides ++= Seq(
      "com.lihaoyi" %% "upickle" % "3.1.0",
      "com.lihaoyi" %% "geny" % "1.0.0"
    )
  )
