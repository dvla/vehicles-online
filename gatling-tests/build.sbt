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

lazy val gatlingVersion = "2.1.1"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-app" % gatlingVersion withSources() withJavadoc(),
  "io.gatling" % "gatling-recorder" % gatlingVersion withSources() withJavadoc(),
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion withSources() withJavadoc(),
  "io.gatling" % "gatling-test-framework" % gatlingVersion withSources() withJavadoc()
)
