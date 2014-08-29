import sbt._
import sbt.Keys._
import Runner._
import ProjectsDefinitions._

object Tasks {
  final val HttpsPort = 17443
  final val OsAddressLookupPort = 17801
  final val VehicleLookupPort = 17802
  final val VehicleDisposePort = 17803
  final val LegacyServicesStubsPort = 17086

  lazy val runLegacyStubs = Def.task {
    runProject(
      fullClasspath.all(scopeLegacyStubs).value.flatten,
      None,
      runJavaMain("service.LegacyServicesRunner", Array(LegacyServicesStubsPort.toString))
    )
  }

  lazy val runOsAddressLookup = Def.task {
    runProject(
      fullClasspath.all(scopeOsAddressLookup).value.flatten,
      Some(ConfigDetails(
        secretRepoLocation((target in ThisProject).value),
        "ms/dev/os-address-lookup.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeOsAddressLookup).value.head, s"${osAddressLookup.id}.conf"),
          setServicePort(OsAddressLookupPort)
        ))
      ))
    )
  }

  lazy val runVehiclesLookup = Def.task {
    runProject(
      fullClasspath.all(scopeVehiclesLookup).value.flatten,
      Some(ConfigDetails(
        secretRepoLocation((target in ThisProject).value),
        "ms/dev/vehicles-lookup.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVehiclesLookup).value.head, s"${vehiclesLookup.id}.conf"),
          setServicePortAndLegacyServicesPort(VehicleLookupPort, "getVehicleDetails.baseurl", LegacyServicesStubsPort)
        ))
      ))
    )
  }

  lazy val runVehiclesDisposeFulfil = Def.task {
    runProject(
      fullClasspath.all(scopeVehiclesDisposeFulfil).value.flatten,
      Some(ConfigDetails(
        secretRepoLocation((target in ThisProject).value),
        "ms/dev/vehicles-dispose-fulfil.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVehiclesDisposeFulfil).value.head, s"${vehiclesDisposeFulfil.id}.conf"),
          setServicePortAndLegacyServicesPort(VehicleDisposePort, "vss.baseurl", LegacyServicesStubsPort)
        ))
      ))
    )
  }

  lazy val runAllMicroservices = Def.task {
    runLegacyStubs.value
    runOsAddressLookup.value
    runVehiclesLookup.value
    runVehiclesDisposeFulfil.value
  }

  lazy val runAppAndMicroservices = Def.task {
    runAllMicroservices.value
    run.in(Compile).toTask("").value
  }

  lazy val testGatling = Def.task {
    val classPath = fullClasspath.all(scopeGatlingTests).value.flatten

    def extractVehiclesGatlingJar(toFolder: File) =
      classPath.find(_.data.toURI.toURL.toString.endsWith(s"vehicles-gatling-$VersionVehiclesGatling.jar"))
        .map { jar => IO.unzip(new File(jar.data.toURI.toURL.getFile), toFolder)}

    val targetFolder = target.in(gatlingTests).value.getAbsolutePath
    val vehiclesGatlingExtractDir = new File(s"$targetFolder/gatlingJarExtract")
    IO.delete(vehiclesGatlingExtractDir)
    vehiclesGatlingExtractDir.mkdirs()
    extractVehiclesGatlingJar(vehiclesGatlingExtractDir)
    System.setProperty("gatling.core.disableCompiler", "true")
    runProject(
      classPath,
      None,
      runJavaMain(
        mainClassName = "io.gatling.app.Gatling",
        args = Array(
          "--simulation", "uk.gov.dvla.SmokeTestSimulation",
          "--data-folder", s"${vehiclesGatlingExtractDir.getAbsolutePath}/data",
          "--results-folder", s"$targetFolder/gatling",
          "--request-bodies-folder", s"$targetFolder/request-bodies"
        ),
        method = "runGatling"
      )
    ) match {
      case 0 => println("Gatling execution SUCCESS")
      case exitCode =>
        println("Gatling execution FAILURE")
        throw new Exception(s"Gatling run exited with error $exitCode")
    }
  }

  lazy val runAsync = Def.task {
    runAsyncHttpsEnvVars.value
    runProject(
      fullClasspath.in(Test).value,
      None,
      runScalaMain("play.core.server.NettyServer", Array((baseDirectory in ThisProject).value.getAbsolutePath))
    )
    System.setProperty("acceptance.test.url", s"https://localhost:$HttpsPort/")
  }

  lazy val runAppAndMicroservicesAsync = Def.task[Unit] {
    runAllMicroservices.value
    runAsync.value
  }

  lazy val allAcceptanceTests = Def.task {
    (test in Test in acceptanceTestsProject).value
    testGatling.value
  }

  lazy val runAsyncHttpsEnvVars = Def.task {
    System.setProperty("https.port", HttpsPort.toString)
    System.setProperty("http.port", "disabled")
    System.setProperty("jsse.enableSNIExtension", "false") // Disable the SNI for testing
    System.setProperty("baseUrl", s"https://localhost:$HttpsPort")
  }

  val setMicroservicesPortsEnvVars = Def.task {
    System.setProperty("ordnancesurvey.baseUrl", s"http://localhost:$OsAddressLookupPort")
    System.setProperty("vehicleLookup.baseUrl", s"http://localhost:$VehicleLookupPort")
    System.setProperty("disposeVehicle.baseUrl", s"http://localhost:$VehicleDisposePort")
  }
}
