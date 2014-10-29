import sbt._

sbtPlugin := true

name := "microservices-sandbox"

version := "1.0.0-SNAPSHOT"

organization := "dvla"

organizationName := "Driver & Vehicle Licensing Agency"

scalaVersion := "2.10.3"

scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-language:reflectiveCalls",
  "-Xmax-classfile-name", "128"
)

val nexus = "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories"

publishTo.<<=(version { v: String =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at s"$nexus/snapshots")
  else
    Some("releases" at s"$nexus/releases")
})

credentials := Seq(Credentials(Path.userHome / ".sbt/.credentials"))

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4" withSources() withJavadoc(),
  "com.typesafe" % "config" % "1.2.1" withSources() withJavadoc()
)

