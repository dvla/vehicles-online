import Common._

name := "vehicles-online-gatling-tests"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

lazy val gatlingVersion = "2.1.7"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-app" % gatlingVersion withSources() withJavadoc(),
  "io.gatling" % "gatling-recorder" % gatlingVersion withSources() withJavadoc(),
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion withSources() withJavadoc(),
  "io.gatling" % "gatling-test-framework" % gatlingVersion withSources() withJavadoc()
)
