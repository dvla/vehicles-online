package pages.disposal_of_vehicle

import models.VehicleLookupFormModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, Page, TelField, TextField, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.VehicleLookup
import VehicleLookup.BackId
import VehicleLookup.ExitId
import VehicleLookup.SubmitId
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.VrmLocked

object VehicleLookupPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/vehicle-lookup"
  final override val title: String = "Enter vehicle details"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TelField = telField(id(DocumentReferenceNumberId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def findVehicleDetails(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(referenceNumber: String = ReferenceNumberValid, registrationNumber: String = RegistrationNumberValid)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber enter referenceNumber
    VehicleLookupPage.vehicleRegistrationNumber enter registrationNumber
    click on findVehicleDetails
  }

  def tryLockedVrm()(implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber enter ReferenceNumberValid
    VehicleLookupPage.vehicleRegistrationNumber enter VrmLocked
    click on findVehicleDetails
  }
}
