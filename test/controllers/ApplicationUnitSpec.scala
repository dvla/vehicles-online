package controllers

import helpers.{TestWithApplication, UnitSpec}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{getProperty, stringProp}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class ApplicationUnitSpec extends UnitSpec {
  "index" should {
    "redirect the user to the start url" in new TestWithApplication {
      implicit val config = configWithStartUrl("/testStart")
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      val result = new ApplicationRoot().index(FakeRequest())
      redirectLocation(result) should equal(Some("/testStart"))
    }

    "redirect the user to the start url when the start url does not have a starting slash" in new TestWithApplication {
      implicit val config = configWithStartUrl("testStart")
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      val result = new ApplicationRoot().index(FakeRequest())
      redirectLocation(result) should equal(Some("testStart"))
    }

    "redirect the user to the start url when the start url has application context" in new TestWithApplication {
      implicit val config = configWithStartUrl("/testContext/testStart")
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      val result = new ApplicationRoot().index(FakeRequest())
      redirectLocation(result) should equal(Some("/testContext/testStart"))
    }
  }

  private def configWithStartUrl(startUrl: String): Config = {
    val config = mock[Config]
    when(config.startUrl).thenReturn(startUrl)
    config
  }
}
