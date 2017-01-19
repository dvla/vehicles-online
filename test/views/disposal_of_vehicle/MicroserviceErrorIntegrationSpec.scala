package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageTitle
import pages.common.AlternateLanguages.{cymraeg, english}
import pages.disposal_of_vehicle.MicroServiceErrorPage.{exit, tryAgain}
import pages.disposal_of_vehicle.{BeforeYouStartPage, MicroServiceErrorPage, SetupTradeDetailsPage, VehicleLookupPage}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag

final class MicroserviceErrorIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to MicroServiceErrorPage
      pageTitle should equal(MicroServiceErrorPage.title)
    }

  }

  "tryAgain button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on tryAgain
      pageTitle should equal(VehicleLookupPage.title)
    }

    "redirect to setuptradedetails when no details are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to MicroServiceErrorPage
      click on tryAgain
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "not be its own referer" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on cymraeg
      click on english
      click on tryAgain
      pageTitle should equal(VehicleLookupPage.title)
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
