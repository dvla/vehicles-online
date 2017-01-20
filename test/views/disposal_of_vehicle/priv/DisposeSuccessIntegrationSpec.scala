package views.disposal_of_vehicle.priv

import composition.TestHarness
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.priv.DisposeSuccessForPrivateKeeperPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
import views.disposal_of_vehicle.DisposeSuccess

class DisposeSuccessIntegrationSpec extends UiSpec with TestHarness {

  "new disposal button" should {
    "be present when the disposal is done by a private keeper" taggedAs UiTag in
      new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessForPrivateKeeperPage
      pageTitle should equal(DisposeSuccessForPrivateKeeperPage.title)
      pageSource should include(s"""id="${DisposeSuccess.NewDisposalId}"""")
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
