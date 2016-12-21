package pages.disposal_of_vehicle.priv

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import webserviceclients.fakes.FakeAddressLookupService.{PostcodeValid, TraderBusinessNameValid}

object SetupTradeDetailsPage extends pages.disposal_of_vehicle.SetupTradeDetailsPageBase {
  final val address = s"$applicationContext/private/setup-trade-details"

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName.value = traderBusinessName
    traderPostcode.value = traderBusinessPostcode
    click on lookup
  }
}