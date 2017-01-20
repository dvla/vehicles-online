package views.disposal_of_vehicle

import composition.TestHarness
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.DisposeFailurePage.{setuptradedetails, vehiclelookup}
import pages.disposal_of_vehicle.{BeforeYouStartPage, DisposeFailurePage, SetupTradeDetailsPage, VehicleLookupPage}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

final class DisposeFailureIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeFailurePage
      pageTitle should equal(DisposeFailurePage.title)
    }

    "redirect to setuptrade details if cache is empty on page load" taggedAs UiTag in new WebBrowserForSelenium {
      go to DisposeFailurePage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }

  "vehiclelookup button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeFailurePage
      click on vehiclelookup
      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  "setuptradedetails button" should {
    "redirect to setuptradedetails" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeFailurePage
      click on setuptradedetails
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      vehicleAndKeeperDetailsModel().
      disposeFormModel().
      disposeTransactionId().
      vehicleRegistrationNumber()
}
