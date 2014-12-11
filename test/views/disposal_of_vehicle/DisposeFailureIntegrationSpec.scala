package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.DisposeFailurePage.{setuptradedetails, vehiclelookup}
import pages.disposal_of_vehicle.{BeforeYouStartPage, DisposeFailurePage, SetupTradeDetailsPage, VehicleLookupPage}

final class DisposeFailureIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeFailurePage

      page.title should equal(DisposeFailurePage.title)
    }

    "redirect to setuptrade details if cache is empty on page load" taggedAs UiTag in new WebBrowser {
      go to DisposeFailurePage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to DisposeFailurePage

      page.source should not contain ProgressBar.div
    }
  }

  "vehiclelookup button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeFailurePage

      click on vehiclelookup

      page.title should equal(VehicleLookupPage.title)
    }
  }

  "setuptradedetails button" should {
    "redirect to setuptradedetails" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeFailurePage

      click on setuptradedetails

      page.title should equal(SetupTradeDetailsPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      vehicleDetailsModel().
      disposeFormModel().
      disposeTransactionId().
      vehicleRegistrationNumber()
}