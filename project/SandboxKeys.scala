import sbt.{settingKey, taskKey}

object SandboxKeys {
  lazy val portOffset = settingKey[Int]("The port offset for all the microservices.")

  lazy val sandbox = taskKey[Unit]("Runs the whole sandbox for manual testing including microservices, webapp and legacy stubs'")
  lazy val sandboxAsync = taskKey[Unit]("Runs the whole sandbox asynchronously for manual testing including microservices, webapp and legacy stubs")
  lazy val gatling = taskKey[Unit]("Runs the gatling tests against the sandbox")
  lazy val accept = taskKey[Unit]("Runs all the acceptance tests against the sandbox.")
}
