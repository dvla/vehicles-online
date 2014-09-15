package controllers.disposal_of_vehicle

import controllers.MicroServiceError
import controllers.disposal_of_vehicle.Common.PrototypeHtml
import helpers.common.CookieHelper
import helpers.{UnitSpec, WithApplication}
import CookieHelper.{fetchCookiesFromHeaders, verifyCookieHasBeenDiscarded}
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import MicroServiceError.MicroServiceErrorRefererCacheKey
import org.mockito.Mockito.when
import pages.disposal_of_vehicle.{DisposePage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, REFERER, contentAsString, defaultAwaitTimeout, status}
import utils.helpers.Config

final class MicroserviceErrorUnitSpec extends UnitSpec {
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
      val microServiceErrorPrototypeNotVisible = new MicroServiceError()

      val result = microServiceErrorPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "write micro service error referer cookie" in new WithApplication {
      val referer = DisposePage.address
      val request = FakeRequest().
        withHeaders(REFERER -> referer)
      // Set the previous page.
      val result = microServiceError.present(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(_.name == MicroServiceErrorRefererCacheKey).get.value should equal(referer)
      }
    }

    "remove the interstitial cookie so we redirect to the page identified in the referer cookie and are not redirected elsewhere" in new WithApplication {
      val referer = DisposePage.address
      val request = FakeRequest().
        withHeaders(REFERER -> referer).
        withCookies(CookieFactoryForUnitSpecs.preventGoingToDisposePage())
      // Set the previous page.
      val result = microServiceError.present(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(_.name == MicroServiceErrorRefererCacheKey).get.value should equal(referer)
        verifyCookieHasBeenDiscarded(PreventGoingToDisposePageCacheKey, cookies)
      }
    }
  }

  "try again" should {
    "redirect to vehicle lookup page when there is no referer" in new WithApplication {
      val request = FakeRequest()
      // No previous page cookie, which can only happen if they wiped their cookies after
      // page presented or they are calling the route directly.
      val result = microServiceError.back(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
    "redirect to previous page and discard the referer cookie" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.microServiceError(DisposePage.address))
      val result = microServiceError.back(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposePage.address))
        val cookies = fetchCookiesFromHeaders(r)
        verifyCookieHasBeenDiscarded(MicroServiceErrorRefererCacheKey, cookies)
      }
    }
  }

  private val microServiceError = injector.getInstance(classOf[MicroServiceError])
  private lazy val present = microServiceError.present(FakeRequest())
}
