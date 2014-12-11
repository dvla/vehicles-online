package views.disposal_of_vehicle

import composition.TestHarness
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.DuplicateDisposalErrorPage
import pages.disposal_of_vehicle.DuplicateDisposalErrorPage.{exit, tryAgain}
import pages.disposal_of_vehicle.MicroServiceErrorPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage

final class DuplicateDisposalIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to DuplicateDisposalErrorPage
      page.title should equal(DuplicateDisposalErrorPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue  {
      go to DuplicateDisposalErrorPage
      page.source should not contain ProgressBar.div
    }
  }

  "tryAgain button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DuplicateDisposalErrorPage
      click on tryAgain
      page.title should equal(VehicleLookupPage.title)
    }

    "redirect to setuptradedetails when no details are cached" taggedAs UiTag in new WebBrowser {
      go to MicroServiceErrorPage
      click on tryAgain
      page.title should equal(SetupTradeDetailsPage.title)
    }
  }

  "exit button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on exit
      page.title should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails()
}
