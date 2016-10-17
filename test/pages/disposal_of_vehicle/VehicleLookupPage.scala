package pages.disposal_of_vehicle

import models.VehicleLookupFormModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.TextField
import WebBrowser.textField
import WebBrowser.TelField
import WebBrowser.telField
import WebBrowser.click
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.VehicleLookup.{BackId, ExitId, ResetTraderDetailsId, SubmitId}
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.VrmLocked

trait VehicleLookupPageBase extends Page {
  def address: String

  final override val title: String = "Enter vehicle details"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TelField = telField(id(DocumentReferenceNumberId))

  def resetTraderDetails(implicit driver: WebDriver): Element = find(id(ResetTraderDetailsId)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def findVehicleDetails(implicit driver: WebDriver): Element = find(id(SubmitId)).get
}

object VehicleLookupPage extends VehicleLookupPageBase {
  final val address = s"$applicationContext/vehicle-lookup"

  def happyPath(referenceNumber: String = ReferenceNumberValid, registrationNumber: String = RegistrationNumberValid)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = referenceNumber
    VehicleLookupPage.vehicleRegistrationNumber.value = registrationNumber
    click on findVehicleDetails
  }

  def tryLockedVrm()(implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = ReferenceNumberValid
    VehicleLookupPage.vehicleRegistrationNumber.value = VrmLocked
    click on findVehicleDetails
  }
}
