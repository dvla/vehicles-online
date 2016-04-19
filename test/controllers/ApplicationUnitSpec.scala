package controllers

import composition.{TestGlobal, WithApplication}
import helpers.UnitSpec
import org.mockito.Mockito.when
import org.specs2.execute.{Result, AsResult}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{getProperty, stringProp}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication
import utils.helpers.Config

class ApplicationUnitSpec extends UnitSpec {
  lazy val applicationContext = getProperty[String]("application.context")

  "index" should {
    "redirect the user to the start url" in new WithApplication {
      implicit val config = configWithStartUrl("/testStart")
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      val result = new ApplicationRoot().index(FakeRequest())
      redirectLocation(result) should equal(Some("/testStart"))
    }

    "redirect the user to the start url when the start url does not have a starting slash" in new WithApplication {
      implicit val config = configWithStartUrl("testStart")
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      val result = new ApplicationRoot().index(FakeRequest())
      redirectLocation(result) should equal(Some("testStart"))
    }

    "redirect the user to the start url when the start url has application context" in new WithApplication {
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