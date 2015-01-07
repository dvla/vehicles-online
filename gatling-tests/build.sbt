import Common._

name := "vehicles-online-gatling-tests"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := "2.11.1"

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

lazy val gatlingVersion = "2.1.1"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-app" % gatlingVersion withSources() withJavadoc() exclude("ch.qos.logback", "logback-classic"),
  "io.gatling" % "gatling-recorder" % gatlingVersion withSources() withJavadoc(),
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion withSources() withJavadoc(),
  "io.gatling" % "gatling-test-framework" % gatlingVersion withSources() withJavadoc(),
  "org.slf4j" % "slf4j-log4j12" % "1.7.7" withSources() withJavadoc()
)
