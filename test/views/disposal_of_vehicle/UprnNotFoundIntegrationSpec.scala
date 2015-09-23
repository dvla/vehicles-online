package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.scalatest.selenium.WebBrowser
import WebBrowser.enter
import WebBrowser.Checkbox
import WebBrowser.checkbox
import WebBrowser.TextField
import WebBrowser.textField
import WebBrowser.TelField
import WebBrowser.telField
import WebBrowser.RadioButton
import WebBrowser.radioButton
import WebBrowser.click
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.UprnNotFoundPage.{manualAddress, setupTradeDetails}
import pages.disposal_of_vehicle.{BeforeYouStartPage, EnterAddressManuallyPage, SetupTradeDetailsPage, UprnNotFoundPage}

final class UprnNotFoundIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to UprnNotFoundPage
      pageTitle should equal(UprnNotFoundPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to UprnNotFoundPage
      pageSource should not contain ProgressBar.div
    }
  }

  "setupTradeDetails button" should {
    "go to setuptradedetails page" taggedAs UiTag in new WebBrowserForSelenium {
      go to UprnNotFoundPage
      click on setupTradeDetails
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }

  "manualAddress button" should {
    "go to manualaddress page after the Manual Address button is clicked and " +
      "trade details have been set up in cache" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.setupTradeDetails()
      go to UprnNotFoundPage
      click on manualAddress
      pageTitle should equal (EnterAddressManuallyPage.title)
    }

    "go to setuptradedetails page when trade details have not been set up in cache" taggedAs UiTag in new WebBrowserForSelenium {
      go to UprnNotFoundPage
      click on manualAddress
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }
}