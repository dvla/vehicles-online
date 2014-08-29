import Runner.{secretProperty, decryptPassword}
import sbt.Keys.target
import sbt.{Def, File, ThisProject}
import scala.sys.process.Process
import Runner.secretRepoLocation

object PrerequisitesCheck {
  private val gitHost = "gitlab.preview-dvla.co.uk"
  val secretRepoUrl = s"git@$gitHost:dvla/secret-vehicles-online.git"

  lazy val prerequisitesCheck = Def.task {
    validatePrerequisites()
    updateSecretVehiclesOnline(secretRepoLocation((target in ThisProject).value))
  }

  private def updateSecretVehiclesOnline(secretRepo: File) {
    val secretRepoLocalPath = secretRepo.getAbsolutePath
    val gitOptions = s"--work-tree $secretRepoLocalPath --git-dir $secretRepoLocalPath/.git"

    if (new File(secretRepo, ".git").exists())
      println(Process(s"git $gitOptions pull origin master").!!<)
    else
      println(Process(s"git clone $secretRepoUrl $secretRepoLocalPath").!!<)
  }

  private def validatePrerequisites() {
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
}
