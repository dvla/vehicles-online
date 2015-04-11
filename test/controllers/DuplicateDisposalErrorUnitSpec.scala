package controllers

import controllers.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout, status}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class DuplicateDisposalErrorUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      status(present) should equal(OK)
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
      val duplicateDisposalErrorPrototypeNotVisible = new DuplicateDisposalError()

      val result = duplicateDisposalErrorPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private lazy val present = {
    val duplicateDisposalError = injector.getInstance(classOf[DuplicateDisposalError])
    val newFakeRequest = FakeRequest()
    duplicateDisposalError.present(newFakeRequest)
  }
}