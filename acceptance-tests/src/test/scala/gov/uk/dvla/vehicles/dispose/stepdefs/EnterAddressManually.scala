package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.EnterAddressManuallyPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class EnterAddressManually(webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper{

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  @Given("^the user is on the manual address page$")
  def the_user_is_on_the_manual_address_page() {
    (new CommonSteps(webBrowserDriver)).goToEnterAddressManuallyPage()
  }

  @When("^the user has selected the submit control with the postcode \"(.*?)\"$")
  def the_user_has_selected_the_submit_control(postcode: String) {
    EnterAddressManuallyPage.addressBuildingNameOrNumber.value = "1 Long Road"
    EnterAddressManuallyPage.addressPostTown.value = "Swansea"
    EnterAddressManuallyPage.addressPostcode.value = postcode
    click on EnterAddressManuallyPage.next
  }

  @Then("^the user is taken to the vehicle lookup page$")
  def the_user_is_taken_to_the_vehicle_lookup_page() {
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
  }

  @Then("^the page will contain text \"(.*?)\"$")
  def the_page_will_contain_text(text: String) {
    pageSource should include(text) withClue trackingId
  }
}
