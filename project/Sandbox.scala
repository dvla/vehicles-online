import java.io.StringReader
import java.net.URLClassLoader
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import sbt.Keys._
import sbt._
import scala.sys.process.Process
import spray.revolver.RevolverPlugin._

object Sandbox extends Plugin {
  final val VersionOsAddressLookup = "0.1-SNAPSHOT"
  final val VersionVehiclesLookup = "0.1-SNAPSHOT"
  final val VersionVehiclesDisposeFulfil = "0.1-SNAPSHOT"
  final val VersionLegacyStubs = "1.0-SNAPSHOT"
  final val VersionJetty = "9.2.1.v20140609"
  final val VersionSpringWeb = "3.0.7.RELEASE"
  final val VersionVehiclesGatling = "1.0-SNAPSHOT"
  final val VersionGatling = "1.0-SNAPSHOT"
  final val VersionGatlingApp = "2.0.0-M4-NAP"

  final val HttpsPort = 17443
  final val OsAddressLookupPort = 17801
  final val VehicleLookupPort = 17802
  final val VehicleDisposePort = 17803

  final val LegacyServicesStubsPort = 17086
  val secretProperty = "DECRYPT_PASSWORD"
  val secretProperty2 = "GIT_SECRET_PASSPHRASE"
  val gitHost = "gitlab.preview-dvla.co.uk"
  val secretRepoUrl = s"git@$gitHost:dvla/secret-vehicles-online.git"

  val decryptPassword = sys.props.get(secretProperty)
    .orElse(sys.env.get(secretProperty))
    .orElse(sys.props.get(secretProperty2))
    .orElse(sys.env.get(secretProperty2))

  def sandProject(name: String, deps: ModuleID*): (Project, ScopeFilter) =
    sandProject(name,  Seq[Resolver](), deps: _*)

  def sandProject(name: String,
                  res: Seq[Resolver],
                  deps: ModuleID*): (Project, ScopeFilter) = (
    Project(name, file(s"target/sandbox/$name"))
      .settings(libraryDependencies ++= deps)
      .settings(resolvers ++= (Common.projectResolvers ++ res))
      .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*),
    ScopeFilter(inProjects(LocalProject(name)), inConfigurations(Runtime))
  )

  lazy val (osAddressLookup, scopeOsAddressLookup) =
    sandProject("os-address-lookup","dvla" %% "os-address-lookup" % VersionOsAddressLookup)
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
  lazy val (gatlingTests, scopeGatlingTests) = sandProject (
    name = "gatling",
    Seq("Central Maven" at "http://central.maven.org/maven2"),
    "com.netaporter.gatling" % "gatling-app" % VersionGatlingApp,
    "uk.gov.dvla" % "vehicles-gatling" % VersionVehiclesGatling
  )

  lazy val sandboxedProjects = Seq(osAddressLookup, vehiclesLookup, vehiclesDisposeFulfil, legacyStubs)

  lazy val vehiclesOnline = ScopeFilter(inProjects(ThisProject), inConfigurations(Runtime))

  lazy val runMicroServices = taskKey[Unit]("Runs all the required by the sandbox micro services'")
  lazy val runMicroServicesTask = runMicroServices := {
    validatePrerequisites()

    val targetFolder = (target in ThisProject).value
    val secretRepoFolder = new File(targetFolder, "secretRepo")
    updateSecretVehiclesOnline(secretRepoFolder)
    runProject(
      fullClasspath.all(scopeOsAddressLookup).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/os-address-lookup.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeOsAddressLookup).value.head, s"${osAddressLookup.id}.conf"),
          setServicePort(OsAddressLookupPort)
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeVehiclesLookup).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/vehicles-lookup.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVehiclesLookup).value.head, s"${vehiclesLookup.id}.conf"),
          setServicePortAndLegacyServicesPort(VehicleLookupPort, "getVehicleDetails.baseurl", LegacyServicesStubsPort)
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeVehiclesDisposeFulfil).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/vehicles-dispose-fulfil.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVehiclesDisposeFulfil).value.head, s"${vehiclesDisposeFulfil.id}.conf"),
          setServicePortAndLegacyServicesPort(VehicleDisposePort, "vss.baseurl", LegacyServicesStubsPort)
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeLegacyStubs).value.flatten,
      None,
      runJavaMain("service.LegacyServicesRunner", Array(LegacyServicesStubsPort.toString))
    )
  }

  lazy val sandbox = taskKey[Unit]("Runs the whole sandbox for manual testing including microservices, webapp and legacy stubs'")
  lazy val sandboxTask = sandbox <<= (runMicroServices, (run in Runtime).toTask("")) { (body, stop) =>
    System.setProperty("ordnancesurvey.baseUrl", s"http://localhost:$OsAddressLookupPort")
    System.setProperty("vehicleLookup.baseUrl", s"http://localhost:$VehicleLookupPort")
    System.setProperty("disposeVehicle.baseUrl", s"http://localhost:$VehicleDisposePort")
    body.flatMap(t => stop)
  }

  lazy val testGatling = taskKey[Unit]("Runs the gatling test")
  lazy val testGatlingTask = testGatling := {
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

  lazy val runAsync = taskKey[Unit]("Runs the play application")
  lazy val runAsyncTask = runAsync := {
    System.setProperty("https.port", HttpsPort.toString)
    System.setProperty("http.port", "disabled")
    System.setProperty("jsse.enableSNIExtension", "false") // Disable the SNI for testing
    System.setProperty("baseUrl", s"https://localhost:$HttpsPort")

    runProject(
        fullClasspath.in(Test).value,
        None,
        runScalaMain("play.core.server.NettyServer", Array((baseDirectory in ThisProject).value.getAbsolutePath))
    )
    System.setProperty("acceptance.test.url", s"https://localhost:$HttpsPort/")
  }

  lazy val sandboxAsync = taskKey[Unit]("Runs the whole sandbox asynchronously for manual testing including microservices, webapp and legacy stubs")
  lazy val sandboxAsyncTask = sandboxAsync <<= (runMicroServices, (runAsync in Runtime).toTask) { (body, stop) =>
    body.flatMap(t => stop)
  }

  lazy val gatling = taskKey[Unit]("Runs the gatling tests against the sandbox")
  lazy val gatlingTask = gatling <<= (sandboxAsync, (testGatling in Runtime).toTask) { (body, stop) =>
    body.flatMap(t => stop)
  }

  override def projectSettings = Seq(
    runMicroServicesTask,
    sandboxTask,
    runAsyncTask,
    testGatlingTask,
    sandboxAsyncTask,
    gatlingTask
  ) ++ Revolver.settings


  def validatePrerequisites() {
    print(s"${scala.Console.YELLOW}Verifying git is installed...${scala.Console.RESET}")
    if (Process("git --version").! != 0) {
      println(s"${scala.Console.RED}FAILED.")
      println(s"You don't have git installed. Please install git and try again${scala.Console.RESET}")
      throw new Exception("You don't have git installed. Please install git and try again")
    }

    print(s"${scala.Console.YELLOW}Verifying there is ssh access to $gitHost ...${scala.Console.RESET}")
    if (Process(s"ssh -T git@$gitHost").! != 0) {
      println(s"${scala.Console.RED}FAILED.")
      println(s"Cannot connect to git@$gitHost. Please check your ssh connection to $gitHost. You might need to import your public key to $gitHost${scala.Console.RESET}")
      throw new Exception(s"Cannot connect to git@$gitHost. Please check your ssh connection to $gitHost.")
    }

    print(s"${scala.Console.YELLOW}Verifying $secretProperty is passed ...${scala.Console.RESET}")
    decryptPassword map(secret => println("done")) orElse {
      println(s"""${scala.Console.RED}FAILED.${scala.Console.RESET}""")
      println(s"""${scala.Console.RED}"$secretProperty" not set. Please set it either as jvm arg of sbt """ +
        s""" "-D$secretProperty='secret'"""" +
        s" or export it in the environment with export $secretProperty='some secret prop' ${scala.Console.RESET}")
      throw new Exception(s""" There is no "$secretProperty" set neither as env variable nor as JVM property """)
    }
  }

  def withClassLoader[T](classLoader: ClassLoader)(code: => T) {
    val currentContextClassLoader = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(classLoader)
    try code
    finally Thread.currentThread().setContextClassLoader(currentContextClassLoader)
  }

  def runScalaMain(mainClassName: String, args: Array[String] = Array[String](), method: String = "main")
                  (prjClassLoader: ClassLoader): Any = withClassLoader[Any](prjClassLoader) {
    import scala.reflect.runtime.universe.{newTermName, runtimeMirror}
    lazy val mirror = runtimeMirror(prjClassLoader)
    val bootSymbol = mirror.staticModule(mainClassName).asModule
    val boot = mirror.reflectModule(bootSymbol).instance
    val mainMethodSymbol = bootSymbol.typeSignature.member(newTermName(method)).asMethod
    val bootMirror = mirror.reflect(boot)
    bootMirror.reflectMethod(mainMethodSymbol).apply(args)
  }

  def runJavaMain(mainClassName: String, args: Array[String] = Array[String](), method: String = "main")
                 (prjClassLoader: ClassLoader): Any = withClassLoader(prjClassLoader) {
    val mainClass = prjClassLoader.loadClass(mainClassName)
    val mainMethod = mainClass.getMethod(method, classOf[Array[String]])
    val mainResult = mainMethod.invoke(null, args)
    return mainResult
  }

  case class ConfigDetails(secretRepo: File,
                           encryptedConfig: String,
                           output: Option[ConfigOutput],
                           systemPropertySetter: String => Unit = a => ())

  case class ConfigOutput(decryptedOutput: File, transform: String => String = a => a)

  def runProject(prjClassPath: Seq[Attributed[File]],
                 configDetails: Option[ConfigDetails],
                 runMainMethod: (ClassLoader) => Any = runScalaMain("dvla.microservice.Boot")): Any = {
    configDetails.map { case ConfigDetails(secretRepo, encryptedConfig, output, systemPropertySetter) =>
      val encryptedConfigFile = new File(secretRepo, encryptedConfig)
      output.map { case ConfigOutput(decryptedOutput, transform)=>
        decryptFile(secretRepo.getAbsolutePath, encryptedConfigFile, decryptedOutput, transform)
      }
    }

    val prjClassloader = new URLClassLoader(
      prjClassPath.map(_.data.toURI.toURL).toArray,
      getClass.getClassLoader.getParent.getParent
    )

    runMainMethod(prjClassloader)
  }

  def updateSecretVehiclesOnline(secretRepo: File) {
    val secretRepoLocalPath = secretRepo.getAbsolutePath
    val gitOptions = s"--work-tree $secretRepoLocalPath --git-dir $secretRepoLocalPath/.git"


    if (new File(secretRepo, ".git").exists())
      println(Process(s"git $gitOptions pull origin master").!!<)
    else
      println(Process(s"git clone $secretRepoUrl $secretRepoLocalPath").!!<)
  }

  def decryptFile(secretRepo: String, encrypted: File, dest: File, decryptedTransform: String => String) {
    val decryptFile = s"$secretRepo/decrypt-file"
    dest.getParentFile.mkdirs()
    val decryptCommand = s"$decryptFile ${encrypted.getAbsolutePath} ${dest.getAbsolutePath} ${decryptPassword.get}"
    Process(decryptCommand).!!<

    val transformedFile = decryptedTransform(FileUtils.readFileToString(dest))
    FileUtils.writeStringToFile(dest, transformedFile)
  }

  def setServicePortAndLegacyServicesPort(servicePort: Int, urlProperty: String, newPort: Int)
                                       (properties: String): String =
    setServicePort(servicePort)(updatePropertyPort(urlProperty, newPort)(properties))

  def setServicePort(servicePort: Int)(properties: String): String = {
    s"""
    |$properties
    |port=$servicePort
    """.stripMargin
  }

  def updatePropertyPort(urlProperty: String, newPort: Int)(properties: String): String = {
    val config = ConfigFactory.parseReader(new StringReader(properties))
    val url = new URL(config.getString(urlProperty))

    val newUrl = new URL(url.getProtocol, url.getHost, newPort, url.getFile).toString

    properties.replace(url.toString, newUrl.toString)
  }

}