package views.disposal_of_vehicle

import composition.TestHarness
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.DuplicateDisposalErrorPage
import pages.disposal_of_vehicle.DuplicateDisposalErrorPage.{exit, tryAgain}
import pages.disposal_of_vehicle.MicroServiceErrorPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

final class DuplicateDisposalIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to DuplicateDisposalErrorPage
      pageTitle should equal(DuplicateDisposalErrorPage.title)
    }

   }

  "tryAgain button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DuplicateDisposalErrorPage
      click on tryAgain
      pageTitle should equal(VehicleLookupPage.title)
    }

    "redirect to setuptradedetails when no details are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to MicroServiceErrorPage
      click on tryAgain
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }

  "exit button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on exit
      pageTitle should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails()
}
