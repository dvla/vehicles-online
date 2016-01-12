package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
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
import pages.disposal_of_vehicle.OnHandlerNotFoundPage.exit
import pages.disposal_of_vehicle.{BeforeYouStartPage, OnHandlerNotFoundPage}

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