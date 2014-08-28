import Common._

name := "vehicles-online-acceptance-tests"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

libraryDependencies ++= Seq(
  "info.cukes" %% "cucumber-scala" % "1.1.7" % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-java" % "1.1.7" % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-picocontainer" % "1.1.7" % "test" withSources() withJavadoc()
)
