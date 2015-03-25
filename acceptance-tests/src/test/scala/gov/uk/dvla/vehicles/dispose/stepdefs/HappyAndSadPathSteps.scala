package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposeFailurePage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}

class HappyAndSadPathSteps(webBrowserDriver: WebBrowserDriver) extends WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToCompleteAndConfirmPage() = {
    go to BeforeYouStartPage
    click on BeforeYouStartPage.startNow
    page.title shouldEqual SetupTradeDetailsPage.title
    SetupTradeDetailsPage.traderName enter "trader1"
    SetupTradeDetailsPage.traderPostcode enter "qq99qq"
    click on SetupTradeDetailsPage.lookup
    page.title shouldEqual BusinessChooseYourAddressPage.title
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    page.title shouldEqual VehicleLookupPage.title
    VehicleLookupPage.vehicleRegistrationNumber enter "A1"
    VehicleLookupPage.documentReferenceNumber enter "11111111111"
    click on VehicleLookupPage.findVehicleDetails
    page.title shouldEqual DisposePage.title
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
    enterValidDisposalDate()
  }

  @Given("^I am on the complete and confirm page$")
  def i_am_on_the_complete_and_confirm_page()  {
    goToCompleteAndConfirmPage()
  }

  @When("^I click on confirm sale button without any validation errors$")
  def i_click_on_confirm_sale_button_without_any_validation_errors() {
    click on DisposePage.dispose
  }

  @Then("^I should be taken to successful summary page$")
  def i_should_be_taken_to_successful_summary_page()  {
    page.title shouldEqual DisposeSuccessPage.title
  }

  @Then("^I can see the details about the vehicle registration number doc ref no date of sale and transaction id$")
  def i_can_see_the_details_about_the_vehicle_registration_number_doc_ref_no_date_of_sale_and_transaction_id() {
    page.source.contains("A1-11111111111") shouldEqual true
  }

  @Given("^I am on the successful summary page$")
  def i_am_on_the_successful_summary_page()  {
    i_am_on_the_complete_and_confirm_page()
    click on DisposePage.dispose
  }

  @Given("^I can see the buy another vehicle and finish button$")
  def i_can_see_the_buy_another_vehicle_and_finish_button()  {
    page.source.contains("Sell another vehicle") shouldEqual true
  }

  @When("^I click on buy another vehicle button$")
  def i_click_on_buy_another_vehicle_button()  {
    click on DisposeSuccessPage.newDisposal
  }

  @Then("^I should be taken to vehicle look up page$")
  def i_should_be_taken_to_vehicle_look_up_page()  {
    page.title shouldEqual VehicleLookupPage.title
  }

  @Given("^I am on the complete and confirm page with failure data$")
  def i_am_on_the_complete_and_confirm_page_with_failure_data() {
    go to BeforeYouStartPage
    click on BeforeYouStartPage.startNow
    page.title shouldEqual SetupTradeDetailsPage.title
    SetupTradeDetailsPage.traderName enter "traer1"
    SetupTradeDetailsPage.traderPostcode enter "qq99qq"
    click on SetupTradeDetailsPage.lookup
    page.title shouldEqual BusinessChooseYourAddressPage.title
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    page.title shouldEqual VehicleLookupPage.title
    VehicleLookupPage.vehicleRegistrationNumber enter "AA11AAC"
    VehicleLookupPage.documentReferenceNumber enter "88888888883"
    click on VehicleLookupPage.findVehicleDetails
    page.title shouldEqual DisposePage.title
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
    enterValidDisposalDate()
  }

  @Then("^I should be taken to failure  page$")
  def i_should_be_taken_to_failure_page()  {
    page.title shouldEqual DisposeFailurePage.title
  }

  @Then("^I can see the details of transaction id with failure screen$")
  def i_can_see_the_details_of_transaction_id_with_failure_screen()  {
    page.source.contains("AA11AAC-88888888883") shouldEqual true
  }

  private def enterValidDisposalDate() {
    DisposePage.dateOfDisposalDay enter "25"
    DisposePage.dateOfDisposalMonth enter "11"
    DisposePage.dateOfDisposalYear enter "2013"
  }
}
