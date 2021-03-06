package pages.disposal_of_vehicle.priv

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object BusinessChooseYourAddressPage extends pages.disposal_of_vehicle.BusinessChooseYourAddressPageBase {
  final val address: String = pages.disposal_of_vehicle.buildAppUrl("private/business-choose-your-address")

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    chooseAddress.value = addressLine
    click on select
  }
}
