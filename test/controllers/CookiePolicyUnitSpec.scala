package controllers

import helpers.UnitSpec
import helpers.WithApplication
import pages.disposal_of_vehicle.CookiePolicyPage
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status

class CookiePolicyUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      val result = cookiePolicy.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(CookiePolicyPage.title)
    }
  }

  private def cookiePolicy = testInjector().getInstance(classOf[CookiePolicy])
}
