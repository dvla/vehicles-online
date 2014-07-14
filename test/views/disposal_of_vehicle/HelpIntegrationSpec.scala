package views.disposal_of_vehicle

import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import mappings.common.Help.HelpCacheKey
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.HelpPage.{back, exit}
import pages.disposal_of_vehicle.{BeforeYouStartPage, HelpPage, VehicleLookupPage}
import pages.common.HelpPanel.help

final class HelpIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page containing correct title" taggedAs UiTag in new WebBrowser {
      go to HelpPage

      page.title should equal(HelpPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to HelpPage

      page.source should not contain ProgressBar.div
    }
  }

  "back button" should {
    "redirect to the users previous page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      click on help

      click on back

      page.title should equal(VehicleLookupPage.title)
    }

    "remove cookie" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      click on help

      click on back

      webDriver.manage().getCookieNamed(HelpCacheKey) should equal(null)
    }
  }

  "exit" should {
    "redirect to the start page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      click on help

      click on exit

      page.title should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails()
}