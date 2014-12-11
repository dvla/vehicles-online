package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import pages.disposal_of_vehicle.UprnNotFoundPage.{manualAddress, setupTradeDetails}
import pages.disposal_of_vehicle.{BeforeYouStartPage, EnterAddressManuallyPage, SetupTradeDetailsPage, UprnNotFoundPage}

final class UprnNotFoundIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to UprnNotFoundPage
      page.title should equal(UprnNotFoundPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to UprnNotFoundPage
      page.source should not contain ProgressBar.div
    }
  }

  "setupTradeDetails button" should {
    "go to setuptradedetails page" taggedAs UiTag in new WebBrowser {
      go to UprnNotFoundPage
      click on setupTradeDetails
      page.title should equal(SetupTradeDetailsPage.title)
    }
  }

  "manualAddress button" should {
    "go to manualaddress page after the Manual Address button is clicked and trade details have been set up in cache" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.setupTradeDetails()
      go to UprnNotFoundPage
      click on manualAddress
      page.title should equal (EnterAddressManuallyPage.title)
    }

    "go to setuptradedetails page when trade details have not been set up in cache" taggedAs UiTag in new WebBrowser {
      go to UprnNotFoundPage
      click on manualAddress
      page.title should equal(SetupTradeDetailsPage.title)
    }
  }
}