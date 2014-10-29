import sbt.Keys.{libraryDependencies, resolvers}
import sbt._

object ProjectsDefinitions {
  final val VersionGatlingApp = "2.0.0-M4-NAP"
  final val VersionVehiclesGatling = "1.0-SNAPSHOT"

  def sandProject(name: String, deps: ModuleID*): Project =
    sandProject(name, Seq[Resolver](), deps: _*)

  def sandProject(name: String,
                  res: Seq[Resolver],
                  deps: ModuleID*): Project =
    Project(name, file(s"target/sandbox/$name"))
      .settings(libraryDependencies ++= deps)
      .settings(resolvers ++= (Common.projectResolvers ++ res))
      .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

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
