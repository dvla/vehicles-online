package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.disposal_of_vehicle.PrivacyPolicyPage

final class PrivacyPolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to PrivacyPolicyPage

      currentUrl should equal(PrivacyPolicyPage.url)
    }
  }
}
