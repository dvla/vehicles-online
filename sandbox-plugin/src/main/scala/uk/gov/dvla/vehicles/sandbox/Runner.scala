package uk.gov.dvla.vehicles.sandbox

import java.io.StringReader
import java.net.{URL, URLClassLoader}
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import sbt.Scoped.{Apply4, Apply2, Apply3}
import sbt.{Attributed, Def, File, Task}
import scala.sys.process.Process

object Runner {
  val secretProperty = "DECRYPT_PASSWORD"
  val secretProperty2 = "GIT_SECRET_PASSPHRASE"
  val decryptPassword = sys.props.get(secretProperty)
    .orElse(sys.env.get(secretProperty))
    .orElse(sys.props.get(secretProperty2))
    .orElse(sys.env.get(secretProperty2))

  type ITask[T]  = Def.Initialize[Task[T]]

  def runSequentially[A, B](a: ITask[A], b: ITask[B]) =
    new Apply2((a, b)).apply((a, b) => a.flatMap(x => b))

  def runSequentially[A, B, C](a: ITask[A], b: ITask[B], c: ITask[C]) =
    new Apply3((a, b, c)).apply((a, b, c) => a.flatMap(x => b.flatMap(x => c)))

//  def runInParallel[A, B](a: ITask[A], b: ITask[B]) =
//    new Apply2((a, b)).apply {(a, b) => a && b}
//
//  def runInParallel[A, B, C, D](a: ITask[A], b: ITask[B], c: ITask[C], d: ITask[D]) =
//    new Apply4((a, b, c, d)).apply {(a, b, c, d) => a.flatMap(x => b.flatMap(x => c.flatMap(x => d)))}

  def secretRepoLocation(targetFolder: File): File =
    new File(targetFolder, "secretRepo")

  def withClassLoader[T](classLoader: ClassLoader)(code: => T) {
    val currentContextClassLoader = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(classLoader)
    try code
    finally Thread.currentThread().setContextClassLoader(currentContextClassLoader)
  }

  def runScalaMain(mainClassName: String, args: Array[String] = Array[String](), method: String = "main")
                  (prjClassLoader: ClassLoader): Any = withClassLoader[Any](prjClassLoader) {
    this.synchronized {
      import scala.reflect.runtime.universe.{newTermName, runtimeMirror}
      lazy val mirror = runtimeMirror(prjClassLoader)
      val bootSymbol = mirror.staticModule(mainClassName).asModule
      val boot = mirror.reflectModule(bootSymbol).instance
      val mainMethodSymbol = bootSymbol.typeSignature.member(newTermName(method)).asMethod
      val bootMirror = mirror.reflect(boot)
      bootMirror.reflectMethod(mainMethodSymbol).apply(args)
    }
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
                 runMainMethod: (ClassLoader) => Any = runJavaMain("dvla.microservice.Boot")): Any = try {
    configDetails.map { case ConfigDetails(secretRepo, encryptedConfig, output, systemPropertySetter) =>
      val encryptedConfigFile = new File(secretRepo, encryptedConfig)
      output.map { case ConfigOutput(decryptedOutput, transform) =>
        decryptFile(secretRepo.getAbsolutePath, encryptedConfigFile, decryptedOutput, transform)
      }
    }

    val prjClassloader = new URLClassLoader(
      prjClassPath.map(_.data.toURI.toURL).toArray,
      getClass.getClassLoader.getParent.getParent
    )

    runMainMethod(prjClassloader)
  } catch {
    case t: Throwable =>
      t.printStackTrace()
      throw t
  }

  def decryptFile(secretRepo: String, encrypted: File, dest: File, decryptedTransform: String => String) {
    val decryptFile = s"$secretRepo/decrypt-file"
    Process(s"chmod +x $decryptFile").!!<
    dest.getParentFile.mkdirs()
    if (!encrypted.exists()) throw new Exception(s"File to be decrypted ${encrypted.getAbsolutePath} doesn't exist!")
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
