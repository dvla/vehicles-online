package pages.disposal_of_vehicle

import models.EnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.TextField
import WebBrowser.textField
import WebBrowser.click
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel.Form.PostcodeId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel
import AddressLinesViewModel.Form.{AddressLinesId, BuildingNameOrNumberId, Line2Id, Line3Id, PostTownId}
import views.disposal_of_vehicle.EnterAddressManually
import EnterAddressManually.{BackId, NextId}
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid

object EnterAddressManuallyPage extends Page {
  final val address = buildAppUrl("enter-address-manually")
  final override val title: String = "Enter address"
  
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def addressBuildingNameOrNumber(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$BuildingNameOrNumberId"))

  def addressLine2(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line2Id"))

  def addressLine3(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line3Id"))

  def addressPostTown(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$PostTownId"))

  def addressPostcode(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_$PostcodeId"))

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def happyPath(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                line2: String = Line2Valid,
                line3: String = Line3Valid,
                postTown: String = PostTownValid,
                postcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressLine2.value = line2
    addressLine3.value = line3
    addressPostTown.value = postTown
    addressPostcode.value = postcode
    click on next
  }

  def happyPathMandatoryFieldsOnly(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                   postTown: String = PostTownValid,
                                   postcode: String = PostcodeValid)
                                  (implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressPostTown.value = postTown
    addressPostcode.value = postcode
    click on next
  }

  def sadPath(implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    click on next
  }
}
