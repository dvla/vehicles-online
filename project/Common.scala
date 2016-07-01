import sbt.Keys.version
import sbt._

object Common {
  val versionString = "1.37-SNAPSHOT"
  val scalaVersionString = "2.10.6"
  val organisationString = "dvla"
  val organisationNameString = "Driver & Vehicle Licensing Agency"
  val nexus = "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories"

  val scalaOptionsSeq = Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-language:reflectiveCalls",
    "-Xmax-classfile-name", "128"
  )

  val projectResolvers = Seq(
    "typesafe repo" at "http://repo.typesafe.com/typesafe/releases",
    "spray repo" at "http://repo.spray.io/",
    "local nexus snapshots" at s"$nexus/snapshots",
    "local nexus releases" at s"$nexus/releases"
  )

  val publishResolver: sbt.Def.Initialize[Option[sbt.Resolver]] = version { v: String =>
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at s"$nexus/snapshots")
    else
      Some("releases" at s"$nexus/releases")
  }

  val sbtCredentials = Credentials(Path.userHome / ".sbt/.credentials")
}
