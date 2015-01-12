package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.After
import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.WebBrowserDriver

final class WebBrowserHooks(webBrowserDriver: WebBrowserDriver) {

  @After
  def quitBrowser() = {
    implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]
    webDriver.quit()
  }
}