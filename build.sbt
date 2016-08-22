connectInput in run := true

val additionalScalacOptions = Seq("-deprecation", "-unchecked", "-feature")

val projectSettings = Seq(
  name := "snakes-and-ladders",
  description := "Snakes and Ladders game",
  version := "1.0",
  scalaVersion := "2.11.8",
  organization := "Sebastian Bach",
  scalacOptions ++= additionalScalacOptions
)

val akkaVersion = "2.4.9"

val dependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

lazy val root = (project in file("."))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= dependencies)
