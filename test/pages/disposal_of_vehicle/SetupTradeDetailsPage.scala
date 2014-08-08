package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.SetupTradeDetails
import SetupTradeDetails.SubmitId
import viewmodels.SetupTradeDetailsViewModel.Form.{TraderNameId, TraderPostcodeId}
import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeAddressLookupService.{PostcodeWithoutAddresses, PostcodeValid, TraderBusinessNameValid}

object SetupTradeDetailsPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/setup-trade-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide trader details"

  def traderName(implicit driver: WebDriver): TextField = textField(id(TraderNameId))

  def traderPostcode(implicit driver: WebDriver): TextField = textField(id(TraderPostcodeId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter traderBusinessName
    traderPostcode enter traderBusinessPostcode
    click on lookup
  }

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter TraderBusinessNameValid
    traderPostcode enter PostcodeWithoutAddresses
    click on lookup
  }
}
