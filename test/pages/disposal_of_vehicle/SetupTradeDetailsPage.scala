package pages.disposal_of_vehicle

import uk.gov.dvla.vehicles.presentation.common.model.SetupTradeDetailsFormModel.Form.{TraderNameId, TraderPostcodeId}
import org.openqa.selenium.WebDriver
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
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.SetupTradeDetails
import SetupTradeDetails.SubmitId
import webserviceclients.fakes.FakeAddressLookupService.PostcodeWithoutAddresses
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid
import webserviceclients.fakes.FakeAddressLookupService.TraderBusinessNameValid

object SetupTradeDetailsPage extends Page {
  final val address = s"$applicationContext/setup-trade-details"
  final override val title: String = "Provide trader details"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
  lazy val cegUrl: String = WebDriverFactory.testUrl + address.substring(1) + "/ceg"

  def traderName(implicit driver: WebDriver): TextField = textField(id(TraderNameId))

  def traderPostcode(implicit driver: WebDriver): TextField = textField(id(TraderPostcodeId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName.value = traderBusinessName
    traderPostcode.value = traderBusinessPostcode
    click on lookup
  }

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName.value = TraderBusinessNameValid
    traderPostcode.value = PostcodeWithoutAddresses
    click on lookup
  }
}
