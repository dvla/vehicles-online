package uk.gov.dvla.vehicles.sandbox

import sbt.Keys.{libraryDependencies, resolvers}
import sbt._

object ProjectDefinitions {
  final val VersionGatlingApp = "2.0.0-M4-NAP"
  final val VersionVehiclesGatling = "1.0-SNAPSHOT"

  private val nexus = "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories"
  private val projectResolvers = Seq(
    "typesafe repo" at "http://repo.typesafe.com/typesafe/releases",
    "spray repo" at "http://repo.spray.io/",
    "local nexus snapshots" at s"$nexus/snapshots",
    "local nexus releases" at s"$nexus/releases"
  )

  def sandProject(name: String, deps: ModuleID*): Project =
    sandProject(name, Seq[Resolver](), deps: _*)

  def sandProject(name: String,
                  res: Seq[Resolver],
                  deps: ModuleID*): Project =
    Project(name, file(s"target/sandbox/$name"))
      .settings(libraryDependencies ++= deps)
      .settings(resolvers ++= (projectResolvers ++ res))

  def osAddressLookup(version: String) =
    sandProject("os-address-lookup", "dvla" %% "os-address-lookup" % version)

  def vehiclesLookup(version: String) =
    sandProject("vehicles-lookup", "dvla" %% "vehicles-lookup" % version)

  def vehiclesDisposeFulfil(version: String) =
    sandProject("vehicles-dispose-fulfil", "dvla" %% "vehicles-dispose-fulfil" % version)

  def legacyStubs(version: String) = sandProject(
    name = "legacy-stubs",
    "dvla-legacy-stub-services" % "legacy-stub-services-service" % version
  )

  def gatlingTests() = sandProject(
    name = "gatling",
    Seq("Central Maven" at "http://central.maven.org/maven2"),
    "com.netaporter.gatling" % "gatling-app" % VersionGatlingApp,
    "uk.gov.dvla" % "vehicles-gatling" % VersionVehiclesGatling
  )
}
