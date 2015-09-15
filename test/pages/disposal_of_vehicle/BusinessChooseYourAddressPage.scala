package pages.disposal_of_vehicle

import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id, singleSel}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
//import helpers.webbrowser.{Element, Page, SingleSel, WebBrowserDSL, WebDriverFactory}
import helpers.webbrowser.{Element, Page, SingleSel, WebDriverFactory}
import views.disposal_of_vehicle.BusinessChooseYourAddress
import BusinessChooseYourAddress.BackId
import BusinessChooseYourAddress.EnterAddressManuallyButtonId
import BusinessChooseYourAddress.SelectId

//object BusinessChooseYourAddressPage extends Page with WebBrowserDSL {
object BusinessChooseYourAddressPage extends Page {
  final val address: String = s"$applicationContext/business-choose-your-address"
  final override val title = "Select trader address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def chooseAddress(implicit driver: WebDriver) = singleSel(id(AddressSelectId))
//  def chooseAddress(implicit driver: WebDriver): SingleSel = singleSel(id(AddressSelectId))

  def back(implicit driver: WebDriver) = find(id(BackId)).get
//  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver) = find(id(EnterAddressManuallyButtonId)).get
//  def manualAddress(implicit driver: WebDriver): Element = find(id(EnterAddressManuallyButtonId)).get

  def getList(implicit driver: WebDriver) = singleSel(id(AddressSelectId)).getOptions

  def getListCount(implicit driver: WebDriver): Int = getList.size

//  def select(implicit driver: WebDriver): Element = find(id(SelectId)).get
  def select(implicit driver: WebDriver) = find(id(SelectId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    // HACK for Northern Ireland
//    chooseAddress.value = traderUprnValid.toString
    chooseAddress.value = "0"
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}
