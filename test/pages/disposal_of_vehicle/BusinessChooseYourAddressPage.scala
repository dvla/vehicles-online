package pages.disposal_of_vehicle

import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.currentUrl
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.singleSel
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.BusinessChooseYourAddress
import BusinessChooseYourAddress.BackId
import BusinessChooseYourAddress.EnterAddressManuallyButtonId
import BusinessChooseYourAddress.SelectId
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.selectedAddress

trait BusinessChooseYourAddressPageBase extends Page {
  def address: String

  final val title = "Select trader address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  val addressLine = "presentationProperty stub, 123, property stub, street stub, town stub, area stub, QQ99QQ"
  final val selectedAddressLine = "Not real street 1, Not real street2, Not real town, QQ9 9QQ"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def chooseAddress(implicit driver: WebDriver) = singleSel(id(AddressSelectId))

  def back(implicit driver: WebDriver) = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver) = find(id(EnterAddressManuallyButtonId)).get

  def select(implicit driver: WebDriver) = find(id(SelectId)).get
}

object BusinessChooseYourAddressPage extends BusinessChooseYourAddressPageBase {
  final val address: String = buildAppUrl("business-choose-your-address")

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    chooseAddress.value = addressLine
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}
