package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser.pageTitle
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WithClue, WebBrowserDriver}

class DemoTestSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers with WithClue {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  @Given("^I am on the vehicles online prototype site url$")
  def i_am_on_the_vehicles_online_prototype_site_url() {
   go to BeforeYouStartPage
  }

  @Given("^I click the Start now button to begin the transaction$")
  def i_click_the_Start_now_button_to_begin_the_transaction() {
   click on BeforeYouStartPage.startNow
  }

  @Given("^I enter trader name and postcode then click on next button$")
  def i_enter_trader_name_and_postcode_then_click_on_next_button() {
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
    SetupTradeDetailsPage.traderName.value = "sudotrader"
    SetupTradeDetailsPage.traderPostcode.value = "qq99qq"
    click on SetupTradeDetailsPage.lookup
  }

  @Given("^Select the address form address choose page then click on next button$")
  def select_the_address_form_address_choose_page_then_click_on_next_button() {
    pageTitle shouldEqual BusinessChooseYourAddressPage.title withClue trackingId
    BusinessChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.selectedAddressLine
    click on BusinessChooseYourAddressPage.select
  }

  @When("^I enter vehicle look up details and click on submit button$")
  def i_enter_vehicle_look_up_details_and_click_on_submit_button() {
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
    VehicleLookupPage.vehicleRegistrationNumber.value = "b1"
    VehicleLookupPage.documentReferenceNumber.value = "11111111111"
    click on VehicleLookupPage.findVehicleDetails
  }

  @Then("^I should be taken to complete and confirm page and fill the required details and click on confirm sale button$")
  def i_should_be_taken_to_complete_and_confirm_page_and_fill_the_required_details_and_click_on_confirm_sale_button() {
    pageTitle shouldEqual DisposePage.title withClue trackingId
  }

  @Then("^I am on the summary page$")
  def i_am_on_the_summary_page()  {
  }
}
