name := "HiitBeats"

version := "0.1"

scalaVersion := "2.13.12"

lazy val example = project.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
	  "org.typelevel" %% "cats-effect" % "3.5.1",

	  // Http4s for building HTTP servers and routes using Ember
	  "org.http4s" %% "http4s-ember-server" % "0.23.18",
	  "org.http4s" %% "http4s-dsl" % "0.23.18",

      "com.comcast" %% "ip4s-core" % "3.2.0",


	  "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M2", 

	  // Play JSON for JSON parsing
	  "com.typesafe.play" %% "play-json" % "2.10.1",

	  // Scala standard library (if not already provided by your Scala version)
	  "org.scala-lang" % "scala-library" % "2.13.12"
     )
  )
