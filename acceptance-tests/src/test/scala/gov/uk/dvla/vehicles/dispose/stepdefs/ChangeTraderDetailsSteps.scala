package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
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

class ChangeTraderDetailsSteps(webBrowserDriver: WebBrowserDriver) extends Matchers with WithClue {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToVehicleDetailsPage() = {
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
  }

  @Given("^I am on the Enter vehicle details page$")
  def the_user_on_enter_vehicle_details_page() {
    goToVehicleDetailsPage()
  }

  @When("^I select the 'Do you wish to change the trader details above\\?' function$")
  def the_user_selects_the_change_trader_details_function()  {
    click on VehicleLookupPage.resetTraderDetails
  }

  @Then("^I will be directed to the Provide Trader details page with the entry fields empty$")
  def the_user_should_taken_to_provide_trader_details_page_with_empty_entry_fields()  {
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
    SetupTradeDetailsPage.traderName.value shouldBe empty
    SetupTradeDetailsPage.traderPostcode.value shouldBe empty
  }
}
