package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given,When,Then}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.disposal_of_vehicle._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}

class BackNavigationSteps(webBrowserDriver:WebBrowserDriver) extends WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToCompleteAndConfirmPage() = {
    go to BeforeYouStartPage
    click on BeforeYouStartPage.startNow
    page.title shouldEqual  SetupTradeDetailsPage.title
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
    page.title shouldEqual  DisposePage.title
    click on DisposePage.useTodaysDate
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("^the user on complete and confirm page without any validation errors$")
  def the_user_on_complete_and_confirm_page_without_any_validation_errors() {
    goToCompleteAndConfirmPage()
  }

  @When("^the user clicks on back button on complete and confirm page$")
  def the_user_clicks_on_back_button_on_complete_and_confirm_page()  {
    click on DisposePage.back
  }

  @Then("^the user should taken to vehicle lookUp Page$")
  def the_user_should_taken_to_vehicle_lookUp_Page()  {
    page.title shouldEqual VehicleLookupPage.title
  }

  @When("^the user click on the back button in vehicle look up page$")
  def the_user_click_on_the_back_button_in_vehicle_look_up_page()  {
    click on VehicleLookupPage.back
  }

  @Then("^navigate to business choose address page$")
  def navigate_to_business_choose_address_page(): Unit =  {
    page.title shouldEqual BusinessChooseYourAddressPage.title
  }

  @When("^the user click on the back button on business choose address page$")
  def the_user_click_on_the_back_button_on_business_choose_address_page()  {
    click on BusinessChooseYourAddressPage.back
  }

  @Then("^the user will navigate to traderDetailsPage$")
  def the_user_will_navigate_to_traderDetailsPage() {
    page.title shouldEqual  SetupTradeDetailsPage.title
  }
}
