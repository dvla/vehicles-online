import sbt.Keys.{libraryDependencies, resolvers}
import sbt._

object ProjectsDefinitions {
  final val VersionOsAddressLookup = "0.1-SNAPSHOT"
  final val VersionVehiclesLookup = "0.1-SNAPSHOT"
  final val VersionVehiclesDisposeFulfil = "0.1-SNAPSHOT"
  final val VersionLegacyStubs = "1.0-SNAPSHOT"
  final val VersionJetty = "9.2.1.v20140609"
  final val VersionSpringWeb = "3.0.7.RELEASE"
  final val VersionVehiclesGatling = "1.0-SNAPSHOT"
  final val VersionGatling = "1.0-SNAPSHOT"
  final val VersionGatlingApp = "2.0.0-M4-NAP"

  def sandProject(name: String, deps: ModuleID*): (Project, ScopeFilter) =
    sandProject(name, Seq[Resolver](), deps: _*)

  def sandProject(name: String,
                  res: Seq[Resolver],
                  deps: ModuleID*): (Project, ScopeFilter) = (
    Project(name, file(s"target/sandbox/$name"))
      .settings(libraryDependencies ++= deps)
      .settings(resolvers ++= (Common.projectResolvers ++ res))
      .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*),
    ScopeFilter(inProjects(LocalProject(name)), inConfigurations(Runtime))
  )

  // Declaring the sandbox projects
  lazy val (osAddressLookup, scopeOsAddressLookup) =
    sandProject("os-address-lookup", "dvla" %% "os-address-lookup" % VersionOsAddressLookup)
  lazy val (vehiclesLookup, scopeVehiclesLookup) =
    sandProject("vehicles-lookup", "dvla" %% "vehicles-lookup" % VersionVehiclesLookup)
  lazy val (vehiclesDisposeFulfil, scopeVehiclesDisposeFulfil) =
    sandProject("vehicles-dispose-fulfil", "dvla" %% "vehicles-dispose-fulfil" % VersionVehiclesDisposeFulfil)
  lazy val (legacyStubs, scopeLegacyStubs) = sandProject(
    name = "legacy-stubs",
    "dvla-legacy-stub-services" % "legacy-stub-services-service" % VersionLegacyStubs,
    "org.eclipse.jetty" % "jetty-server" % VersionJetty,
    "org.eclipse.jetty" % "jetty-servlet" % VersionJetty,
    "org.springframework" % "spring-web" % VersionSpringWeb
  )
  lazy val (gatlingTests, scopeGatlingTests) = sandProject(
    name = "gatling",
    Seq("Central Maven" at "http://central.maven.org/maven2"),
    "com.netaporter.gatling" % "gatling-app" % VersionGatlingApp,
    "uk.gov.dvla" % "vehicles-gatling" % VersionVehiclesGatling
  )

  lazy val vehiclesOnline = ScopeFilter(inProjects(ThisProject), inConfigurations(Runtime))
}
