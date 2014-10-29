import sbt.Keys.{libraryDependencies, resolvers}
import sbt._

object ProjectsDefinitions {
  final val VersionOsAddressLookup = "0.4-SNAPSHOT"
  final val VersionVehiclesLookup = "0.3-SNAPSHOT"
  final val VersionVehiclesDisposeFulfil = "0.3-SNAPSHOT"
  final val VersionLegacyStubs = "1.0-SNAPSHOT"
  final val VersionJetty = "9.2.1.v20140609"
  final val VersionSpringWeb = "3.0.7.RELEASE"
  final val VersionVehiclesGatling = "1.0-SNAPSHOT"
  final val VersionGatling = "1.0-SNAPSHOT"
  final val VersionGatlingApp = "2.0.0-M4-NAP"

  def sandProject(name: String, deps: ModuleID*): Project =
    sandProject(name, Seq[Resolver](), deps: _*)

  def sandProject(name: String,
                  res: Seq[Resolver],
                  deps: ModuleID*): Project =
    Project(name, file(s"target/sandbox/$name"))
      .settings(libraryDependencies ++= deps)
      .settings(resolvers ++= (Common.projectResolvers ++ res))
      .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

  // Declaring the sandbox projects
  lazy val osAddressLookup =
    sandProject("os-address-lookup", "dvla" %% "os-address-lookup" % VersionOsAddressLookup)

  lazy val vehiclesLookup =
    sandProject("vehicles-lookup", "dvla" %% "vehicles-lookup" % VersionVehiclesLookup)

  lazy val vehiclesDisposeFulfil =
    sandProject("vehicles-dispose-fulfil", "dvla" %% "vehicles-dispose-fulfil" % VersionVehiclesDisposeFulfil)

  lazy val legacyStubs = sandProject(
    name = "legacy-stubs",
    "dvla-legacy-stub-services" % "legacy-stub-services-service" % VersionLegacyStubs,
    "org.eclipse.jetty" % "jetty-server" % VersionJetty,
    "org.eclipse.jetty" % "jetty-servlet" % VersionJetty,
    "org.springframework" % "spring-web" % VersionSpringWeb
  )
  lazy val gatlingTests = sandProject(
    name = "gatling",
    Seq("Central Maven" at "http://central.maven.org/maven2"),
    "com.netaporter.gatling" % "gatling-app" % VersionGatlingApp,
    "uk.gov.dvla" % "vehicles-gatling" % VersionVehiclesGatling
  )

  lazy val vehiclesOnline = ScopeFilter(inProjects(ThisProject), inConfigurations(Runtime))
}
