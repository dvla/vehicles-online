package utils.helpers

import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.webbrowser.TestGlobal
import helpers.{UnitSpec, WithApplication}
import pages.disposal_of_vehicle.BeforeYouStartPage
import play.api.test.Helpers.LOCATION
import play.api.test.{FakeApplication, FakeRequest}
import viewmodels.AllCacheKeys

final class CookieHelperSpec extends UnitSpec {
  "handleApplicationSecretChange" should {
    "discard all cookies except SeenCookieMessageKey" in new WithApplication(app = appWithCryptpConfig) {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.seenCookieMessage()).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel())

      val result = CookieHelper.discardAllCookies(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.filter(cookie => AllCacheKeys.contains(cookie.name)).foreach { cookie =>
          cookie.maxAge match {
            case Some(maxAge) if maxAge < 0 => // Success
            case Some(maxAge) => fail(s"maxAge should be negative but was $maxAge")
            case _ => fail("should be some maxAge")
          }
        }
      }
    }

    "redirect to BeforeYouStart page" in new WithApplication(app = appWithCryptpConfig) {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.seenCookieMessage()).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel())

      val result = CookieHelper. discardAllCookies(request)
      
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private val appWithCryptpConfig = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("application.secret256Bit" -> "MnPSvGpiEF5OJRG3xLAnsfmdMTLr6wpmJmZLv2RB9Vo="))
}
