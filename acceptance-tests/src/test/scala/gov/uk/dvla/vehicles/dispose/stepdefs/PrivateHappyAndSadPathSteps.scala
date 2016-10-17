package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.priv.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage
import pages.disposal_of_vehicle.priv.DisposeSuccessForPrivateKeeperPage
import pages.disposal_of_vehicle.priv.NotifyAnotherSalePage
import pages.disposal_of_vehicle.priv.SetupTradeDetailsPage
import pages.disposal_of_vehicle.priv.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import java.util.Calendar

class PrivateHappyAndSadPathSteps(webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper{

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToCompleteAndConfirmPage() = {
    go to SetupTradeDetailsPage
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
    SetupTradeDetailsPage.traderName.value = "trader1"
    SetupTradeDetailsPage.traderPostcode.value = "qq99qq"
    click on SetupTradeDetailsPage.lookup
    pageTitle shouldEqual BusinessChooseYourAddressPage.title withClue trackingId
    BusinessChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.selectedAddressLine
    click on BusinessChooseYourAddressPage.select
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
    VehicleLookupPage.vehicleRegistrationNumber.value = "A1"
    VehicleLookupPage.documentReferenceNumber.value = "11111111111"
    click on VehicleLookupPage.findVehicleDetails
    pageTitle shouldEqual DisposeForPrivateKeeperPage.title withClue trackingId
    click on DisposeForPrivateKeeperPage.lossOfRegistrationConsent
    click on DisposeForPrivateKeeperPage.emailInvisible
    enterValidDisposalDate()
  }

  @Given("^I am on the private complete and confirm page$")
  def i_am_on_the_private_complete_and_confirm_page()  {
    goToCompleteAndConfirmPage()
  }

  @Then("^I should be taken to the private successful summary page$")
  def i_should_be_taken_to_the_private_successful_summary_page()  {
    pageTitle shouldEqual DisposeSuccessForPrivateKeeperPage.title withClue trackingId
  }

  @Given("^I am on the private successful summary page$")
  def i_am_on_the_private_successful_summary_page()  {
    i_am_on_the_private_complete_and_confirm_page()
    click on DisposeForPrivateKeeperPage.dispose
  }

  @Given("^I can see the notify another sale and finish button$")
  def i_can_see_the_notify_another_sale_and_finish_button()  {
    pageSource.contains("Notify another sale") shouldEqual true withClue trackingId
  }

  @When("^I click on notify another sale button$")
  def i_click_on_notify_another_sale_button()  {
    click on DisposeSuccessForPrivateKeeperPage.newDisposal
  }

  @Then("^I should be taken to notify another sale page$")
  def i_should_be_taken_to_notify_another_sale_page()  {
    pageTitle shouldEqual NotifyAnotherSalePage.title withClue trackingId
  }

  @Then("^I should be taken to setup trade details page$")
  def i_should_be_taken_to_setup_trade_details_page() {
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
  }

  @Given("^I am on the private notify another sale page$")
  def i_am_on_the_private_notify_another_sale_page()  {
    goToCompleteAndConfirmPage()
    click on DisposeForPrivateKeeperPage.dispose
    click on DisposeSuccessForPrivateKeeperPage.newDisposal
    pageTitle shouldEqual NotifyAnotherSalePage.title withClue trackingId
  }

  @When("""^I select "([^"]*)" and click next$""")
  def i_select_and_click_next(select:String) {
    if (select == "Yes")
      click on NotifyAnotherSalePage.yes
    else
      click on NotifyAnotherSalePage.no
    click on NotifyAnotherSalePage.next
  }

  @Then("^the trader details should be played back$")
  def the_trader_details_should_be_played_back() {
    val source = pageSource
    pageSource.contains("TRADER1") shouldEqual true
    pageSource.contains("QQ9 9QQ") shouldEqual true
  }

  @Then("^trader input fields should be blank$")
  def trader_input_fields_should_be_blank() {
    SetupTradeDetailsPage.traderName.value shouldEqual ""
    SetupTradeDetailsPage.traderPostcode.value shouldEqual ""
  }


  @Given("^I am on the private complete and confirm page with failure data$")
  def i_am_on_the_private_complete_and_confirm_page_with_failure_data() {
    go to SetupTradeDetailsPage
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
    SetupTradeDetailsPage.traderName.value = "traer1"
    SetupTradeDetailsPage.traderPostcode.value = "qq99qq"
    click on SetupTradeDetailsPage.lookup
    pageTitle shouldEqual BusinessChooseYourAddressPage.title withClue trackingId
    BusinessChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.selectedAddressLine
    click on BusinessChooseYourAddressPage.select
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
    VehicleLookupPage.vehicleRegistrationNumber.value = "AA11AAC"
    VehicleLookupPage.documentReferenceNumber.value = "88888888883"
    click on VehicleLookupPage.findVehicleDetails
    pageTitle shouldEqual DisposeForPrivateKeeperPage.title withClue trackingId
    click on DisposeForPrivateKeeperPage.lossOfRegistrationConsent
    click on DisposeForPrivateKeeperPage.emailInvisible
    enterValidDisposalDate()
  }

  private def enterValidDisposalDate() {
    // todays's date
    val today = Calendar.getInstance()
    DisposeForPrivateKeeperPage.dateOfDisposalDay.value = f"${today.get(Calendar.DATE)}%02d"
    DisposeForPrivateKeeperPage.dateOfDisposalMonth.value = f"${today.get(Calendar.MONTH)+1}%02d"
    DisposeForPrivateKeeperPage.dateOfDisposalYear.value = today.get(Calendar.YEAR).toString
  }
}
