package controllers

import Common.PrototypeHtml
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import pages.disposal_of_vehicle.ErrorPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class ErrorUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
      when(config.assetsUrl).thenReturn(None) // Stub this config value.
      val errorPrototypeNotVisible = new controllers.Error()

      val result = errorPrototypeNotVisible.present(ErrorPage.exceptionDigest)(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  // TODO please add test for 'submit'.

  private lazy val errorController = injector.getInstance(classOf[controllers.Error])

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
      withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    errorController.present(ErrorPage.exceptionDigest)(request)
  }
}