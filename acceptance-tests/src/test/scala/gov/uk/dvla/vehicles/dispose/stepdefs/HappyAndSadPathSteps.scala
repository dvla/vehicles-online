package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import org.scalatest.Matchers
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposeFailurePage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WithClue, WebBrowserDriver}

class HappyAndSadPathSteps(webBrowserDriver: WebBrowserDriver) extends Matchers with WithClue{

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToCompleteAndConfirmPage() = {
    go to BeforeYouStartPage
    click on BeforeYouStartPage.startNow
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
    SetupTradeDetailsPage.traderName.value = "trader1"
    SetupTradeDetailsPage.traderPostcode.value = "qq99qq"
    click on SetupTradeDetailsPage.lookup
    pageTitle shouldEqual BusinessChooseYourAddressPage.title withClue trackingId
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
    VehicleLookupPage.vehicleRegistrationNumber.value = "A1"
    VehicleLookupPage.documentReferenceNumber.value = "11111111111"
    click on VehicleLookupPage.findVehicleDetails
    pageTitle shouldEqual DisposePage.title withClue trackingId
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
    pageTitle shouldEqual DisposeSuccessPage.title withClue trackingId
  }

  @Then("^I can see the details about the vehicle registration number doc ref no date of sale and transaction id$")
  def i_can_see_the_details_about_the_vehicle_registration_number_doc_ref_no_date_of_sale_and_transaction_id() {
    pageSource.contains("A1-11111111111") shouldEqual true withClue trackingId
  }

  @Given("^I am on the successful summary page$")
  def i_am_on_the_successful_summary_page()  {
    i_am_on_the_complete_and_confirm_page()
    click on DisposePage.dispose
  }

  @Given("^I can see the buy another vehicle and finish button$")
  def i_can_see_the_buy_another_vehicle_and_finish_button()  {
    pageSource.contains("Notify Another Sale") shouldEqual true withClue trackingId
  }

  @When("^I click on buy another vehicle button$")
  def i_click_on_buy_another_vehicle_button()  {
    click on DisposeSuccessPage.newDisposal
  }

  @Then("^I should be taken to vehicle look up page$")
  def i_should_be_taken_to_vehicle_look_up_page()  {
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
  }

    @Given("^I am on the complete and confirm page with failure data$")
  def i_am_on_the_complete_and_confirm_page_with_failure_data() {
    go to BeforeYouStartPage
    click on BeforeYouStartPage.startNow
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
    SetupTradeDetailsPage.traderName.value = "traer1"
    SetupTradeDetailsPage.traderPostcode.value = "qq99qq"
    click on SetupTradeDetailsPage.lookup
    pageTitle shouldEqual BusinessChooseYourAddressPage.title withClue trackingId
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
    VehicleLookupPage.vehicleRegistrationNumber.value = "AA11AAC"
    VehicleLookupPage.documentReferenceNumber.value = "88888888883"
    click on VehicleLookupPage.findVehicleDetails
    pageTitle shouldEqual DisposePage.title withClue trackingId
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
    enterValidDisposalDate()
  }

  @Then("^I should be taken to failure  page$")
  def i_should_be_taken_to_failure_page()  {
    pageTitle shouldEqual DisposeFailurePage.title withClue trackingId
  }

  @Then("^I can see the details of transaction id with failure screen$")
  def i_can_see_the_details_of_transaction_id_with_failure_screen()  {
    pageSource.contains("AA11AAC-88888888883") shouldEqual true withClue trackingId
  }

  private def enterValidDisposalDate() {
    DisposePage.dateOfDisposalDay.value = "25"
    DisposePage.dateOfDisposalMonth.value = "11"
    DisposePage.dateOfDisposalYear.value = "2013"
  }
}
