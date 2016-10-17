package pages.disposal_of_vehicle.priv

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}

object VehicleLookupPage extends pages.disposal_of_vehicle.VehicleLookupPageBase {
  final val address = s"$applicationContext/private/vehicle-lookup"

  def happyPath(referenceNumber: String = ReferenceNumberValid, registrationNumber: String = RegistrationNumberValid)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = referenceNumber
    VehicleLookupPage.vehicleRegistrationNumber.value = registrationNumber
    click on findVehicleDetails
  }
}
