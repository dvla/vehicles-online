import Common._

name := "vehicles-online-acceptance-tests"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

lazy val cucumberV = "1.2.4"

libraryDependencies ++= Seq(
  "info.cukes" %% "cucumber-scala" % cucumberV % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-java" % cucumberV % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-picocontainer" % cucumberV % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-junit" % cucumberV % "test" withSources() withJavadoc()
)
