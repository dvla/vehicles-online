package helpers.hooks

import composition.TestGlobal
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

//import cucumber.api.java.{After, Before}
import play.api.test.{FakeApplication, TestServer}

final class TestServerHooks {
  import helpers.hooks.TestServerHooks._
  private val testServer: TestServer = TestServer(port = port, application = fakeAppWithTestGlobal)

  //Before(order = 500)
  def startServer() = {
    testServer.start()
  }

 // @After(order = 500)
  def stopServer() = {
    testServer.stop()
  }
}

object TestServerHooks {
  private final val port: Int = 9002
  private lazy val fakeAppWithTestGlobal: FakeApplication = LightFakeApplication.create(TestGlobal)
}