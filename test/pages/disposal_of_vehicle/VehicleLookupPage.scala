package pages.disposal_of_vehicle

import helpers.webbrowser._
import views.disposal_of_vehicle.VehicleLookup
import VehicleLookup.BackId
import viewmodels.VehicleLookupFormViewModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import VehicleLookup.ExitId
import VehicleLookup.SubmitId
import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.VrmLocked

object VehicleLookupPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/vehicle-lookup"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter vehicle details"

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
