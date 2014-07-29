package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, SingleSel, WebBrowserDSL, WebDriverFactory}
import viewmodels.BusinessChooseYourAddressViewModel.Form.AddressSelectId
import mappings.disposal_of_vehicle.BusinessChooseYourAddress.BackId
import mappings.disposal_of_vehicle.BusinessChooseYourAddress.EnterAddressManuallyButtonId
import mappings.disposal_of_vehicle.BusinessChooseYourAddress.SelectId
import org.openqa.selenium.WebDriver
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid

object BusinessChooseYourAddressPage extends Page with WebBrowserDSL {
  final val address: String = "/sell-to-the-trade/business-choose-your-address"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Select your trader address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  def chooseAddress(implicit driver: WebDriver): SingleSel = singleSel(id(AddressSelectId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(EnterAddressManuallyButtonId)).get

  def getList(implicit driver: WebDriver) = singleSel(id(AddressSelectId)).getOptions

  def getListCount(implicit driver: WebDriver): Int = getList.size

  def select(implicit driver: WebDriver): Element = find(id(SelectId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    chooseAddress.value = traderUprnValid.toString
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}
