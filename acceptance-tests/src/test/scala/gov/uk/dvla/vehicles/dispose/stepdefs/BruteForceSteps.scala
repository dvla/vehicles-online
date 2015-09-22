package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser.pageTitle
import org.scalatest.selenium.WebBrowser.click
import pages.disposal_of_vehicle.{DisposePage, VehicleLookupFailurePage, VehicleLookupPage, VrmLockedPage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WithClue, WebBrowserDriver}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

class BruteForceSteps(webBrowserDriver: WebBrowserDriver) extends Matchers with WithClue {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]
  val commonSteps = new CommonSteps(webBrowserDriver)
  final val VrmLocked = RandomVrmGenerator.uniqueVrm // This reg will lock so it can only be used once and cannot be used by any other test
  final val VrmNotLocked = RandomVrmGenerator.uniqueVrm // This reg will not lock so it can be used again after a successful lookup
  final val DocRefNumberSuccessfulLookup = "1" * 11
  final val DocRefNumberUnsuccessfulLookup = "1" * 10 + "2"

  private def lookupVehicle(vrm: String, docRefNumber: String, expectedPageTitle: String) = {
      VehicleLookupPage.vehicleRegistrationNumber.value = vrm
    VehicleLookupPage.documentReferenceNumber.value = docRefNumber
    click on VehicleLookupPage.findVehicleDetails
    pageTitle shouldEqual expectedPageTitle withClue trackingId
  }

  @Given("""^the user is on the vehicle lookup page$""")
  def the_user_is_on_the_vehicle_lookup_page() = {
    commonSteps.goToVehicleLookupPage()
  }

  @When("^the user enters an incorrect vrm & doc ref number combination three times$")
  def the_user_enters_an_incorrect_vrm_doc_ref_number_combination_three_times() = {
    for (attempt <- 1 to 3) {
      lookupVehicle(VrmLocked, DocRefNumberUnsuccessfulLookup, VehicleLookupFailurePage.title)
      click on VehicleLookupFailurePage.vehicleLookup
      pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
    }
  }

  @When("^the user enters an incorrect vrm & doc ref number combination two times$")
  def the_user_enters_an_incorrect_vrm_doc_ref_number_combination_two_times() = {
    for (attempt <- 1 to 2) {
      lookupVehicle(VrmNotLocked, DocRefNumberUnsuccessfulLookup, VehicleLookupFailurePage.title)
      click on VehicleLookupFailurePage.vehicleLookup
      pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
    }
  }

  @Then("^on the fourth attempt the vrm is locked$")
  def on_the_fourth_attempt_the_vrm_is_locked() = {
    lookupVehicle(VrmLocked, DocRefNumberUnsuccessfulLookup, VrmLockedPage.title)
  }

  @Then("""^on the third attempt the vehicle is found and the user can progress to the next page$""")
  def on_the_third_attempt_the_vehicle_is_found_and_the_user_can_progress_to_the_next_page() = {
    VehicleLookupPage.vehicleRegistrationNumber.value = VrmNotLocked
    VehicleLookupPage.documentReferenceNumber.value = DocRefNumberSuccessfulLookup
    click on VehicleLookupPage.findVehicleDetails
    pageTitle should equal(DisposePage.title) withClue trackingId
  }
}
