package controllers.disposal_of_vehicle

import controllers.SoapEndpointError
import controllers.disposal_of_vehicle.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class SoapEndpointErrorUnitSpec extends UnitSpec {
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
      val soapEndpointErrorPrototypeNotVisible = new SoapEndpointError()

      val result = soapEndpointErrorPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private lazy val soapEndpointError = injector.getInstance(classOf[SoapEndpointError])
  private lazy val present = soapEndpointError.present(FakeRequest())
}