package helpers.steps.hooks

import cucumber.api.java.{After, Before}
import helpers.webbrowser.TestGlobal
import play.api.test.{FakeApplication, TestServer}

final class TestServerHooks {
  import TestServerHooks._
  private val testServer: TestServer = TestServer(port = port, application = fakeAppWithTestGlobal)

  @Before(order = 500)
  def startServer() = {
    testServer.start()
  }

  @After(order = 500)
  def stopServer() = {
    testServer.stop()
  }
}

object TestServerHooks {
  private final val port: Int = 9002
  private lazy val fakeAppWithTestGlobal: FakeApplication = FakeApplication(withGlobal = Some(TestGlobal))
}