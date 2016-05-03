package controllers

import helpers.TestWithApplication
import helpers.UnitSpec
import pages.disposal_of_vehicle.TermsAndConditionsPage
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.OK
import play.api.test.Helpers.status

class TermsAndConditionsUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      val result = termsAndConditions.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(TermsAndConditionsPage.title)
    }
  }

  private def termsAndConditions = testInjector().getInstance(classOf[TermsAndConditions])
}
