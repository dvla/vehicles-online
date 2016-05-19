package views.disposal_of_vehicle.priv

import composition.TestHarness
import helpers.UiSpec
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.{DisposeSuccessForPrivateKeeperPage, BeforeYouStartPage}
import views.disposal_of_vehicle.DisposeSuccess

class DisposeSuccessIntegrationSpec extends UiSpec with TestHarness {

  "new disposal button" should {
    "not be present when the disposal is done by a private keeper instead of the trade" taggedAs UiTag in
      new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessForPrivateKeeperPage
      pageTitle should equal(DisposeSuccessForPrivateKeeperPage.title)
      pageSource should not include s"""id="$DisposeSuccess.NewDisposalId""""
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      businessChooseYourAddress().
      enterAddressManually().
      dealerDetails().
      vehicleAndKeeperDetailsModel().
      privateDisposeFormModel().
      disposeTransactionId().
      vehicleRegistrationNumber().
      disposeFormTimestamp()
}
