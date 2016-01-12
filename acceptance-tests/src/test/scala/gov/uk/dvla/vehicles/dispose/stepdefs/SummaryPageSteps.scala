package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.Given
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.disposal_of_vehicle.{VehicleLookupPage, DisposePage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class SummaryPageSteps (webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper {
  implicit lazy val webDriver = webBrowserDriver.asInstanceOf[WebDriver]
  val commonSteps = new CommonSteps(webBrowserDriver)

  @Given("""^details are entered that correspond to a vehicle that has a valid clean record and has no markers or error codes$""")
  def details_are_entered_that_correspond_to_a_vehicle_that_has_a_valid_clean_record_and_has_no_markers_or_error_codes() = {
    commonSteps.goToDisposePage()
    DisposePage.mileage.value = "10000"
    commonSteps.selectTodaysDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^details are entered that correspond to a vehicle that has a valid record but does have markers or error codes$""")
  def details_are_entered_that_correspond_to_a_vehicle_that_has_a_valid_record_but_does_have_markers_or_error_codes() = {
    commonSteps.goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber.value = "AA11AAC" // transaction failure, error code U0020
    VehicleLookupPage.documentReferenceNumber.value = "88888888883" // transaction failure, error code U0020
    click on VehicleLookupPage.findVehicleDetails
    pageTitle should equal(DisposePage.title) withClue trackingId
    DisposePage.mileage.value = "10000"
    commonSteps.selectTodaysDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }


}
