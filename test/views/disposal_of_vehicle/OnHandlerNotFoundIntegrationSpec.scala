package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.OnHandlerNotFoundPage.exit
import pages.disposal_of_vehicle.{BeforeYouStartPage, OnHandlerNotFoundPage}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag

final class OnHandlerNotFoundIntegrationSpec extends UiSpec with TestHarness {
  "go to not found page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to OnHandlerNotFoundPage
      pageTitle should equal(OnHandlerNotFoundPage.title)
    }

  }

  "exit" should {
    "redirect to BeforeYouStartPage" taggedAs UiTag in new WebBrowserForSelenium {
      go to OnHandlerNotFoundPage
      click on exit
      pageTitle should equal(BeforeYouStartPage.title)
    }
  }
}
