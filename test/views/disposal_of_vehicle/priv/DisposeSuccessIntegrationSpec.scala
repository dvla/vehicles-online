package views.disposal_of_vehicle.priv

import composition.TestHarness
import helpers.UiSpec
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.{DisposeSuccessForPrivateKeeperPage, BeforeYouStartPage}
import views.disposal_of_vehicle.DisposeSuccess

class DisposeSuccessIntegrationSpec extends UiSpec with TestHarness {

  "new disposal button" should {
    "not be present when the disposal is done by a private keeper instead of the trade" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessForPrivateKeeperPage
      page.title should equal(DisposeSuccessForPrivateKeeperPage.title)
      page.source should not include s"""id="$DisposeSuccess.NewDisposalId""""
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      businessChooseYourAddress().
      enterAddressManually().
      dealerDetails().
      vehicleAndKeeperDetailsModel().
      disposeFormPrivateModel().
      disposeTransactionId().
      vehicleRegistrationNumber().
      disposeFormTimestamp()
}
