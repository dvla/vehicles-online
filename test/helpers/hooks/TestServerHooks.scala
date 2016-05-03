package helpers.hooks

import composition.TestGlobalWithFilters
import play.api.test.{FakeApplication, TestServer}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

final class TestServerHooks {
  import helpers.hooks.TestServerHooks.{fakeAppWithTestGlobal, port}
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
  private lazy val fakeAppWithTestGlobal: FakeApplication = LightFakeApplication(TestGlobalWithFilters)
}