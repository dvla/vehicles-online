package controllers

import org.mockito.Mockito.when
import org.specs2.execute.{Result, AsResult}
import play.api.test.{Helpers, WithApplication, FakeApplication, FakeRequest}
import play.api.test.Helpers.{redirectLocation, defaultAwaitTimeout}
import helpers.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import utils.helpers.Config

final class ApplicationUnitSpec extends UnitSpec {
  val applicationContext = getProperty("application.context", default = "")

  "index" should {

    "redirect the user to the start url" in new WithApplication {
      implicit val config = configWithStartUrl("/testStart")
      val result = new ApplicationRoot().index(FakeRequest())
      redirectLocation(result) should equal(Some("/testStart"))
    }

    "redirect the user to the start url when the start url does not have a starting slash" in new WithApplication {
      implicit val config = configWithStartUrl("testStart")
      val result = new ApplicationRoot().index(FakeRequest())
      redirectLocation(result) should equal(Some("testStart"))
    }

    "redirect the user to the start url when the start url has application context" in new WithApplication {
        implicit val config = configWithStartUrl("/testContext/testStart")
        val result = new ApplicationRoot().index(FakeRequest())
        redirectLocation(result) should equal(Some("/testContext/testStart"))
    }
  }

  // This Application allows us to safely apply an application context and ensure that the changes are reverted
  // once the test has finished executing. This is because the default Router object contains global shared state
  class WithApplicationContext(context: String) extends play.api.test.WithApplication (
    app = FakeApplication(additionalConfiguration = Map("application.context" -> context))
  ){
    override def around[T: AsResult](t: => T): Result =
    {
      Helpers.running(app) {
        try AsResult.effectively(t)
        finally app.routes.map(_.setPrefix("/"))
      }
    }
  }

  private def configWithStartUrl(startUrl: String): Config = {
    val config = mock[Config]
    when(config.startUrl).thenReturn(startUrl)
    config
  }
}