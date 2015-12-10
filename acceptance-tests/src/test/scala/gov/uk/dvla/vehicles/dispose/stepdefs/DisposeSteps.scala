package gov.uk.dvla.vehicles.dispose.stepdefs

import java.util.Calendar

import cucumber.api.java.en.{Given, When, Then}
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser.pageTitle
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import pages.common.ErrorPanel
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DisposePage.dispose
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WithClue, WebBrowserDriver}

class DisposeSteps(webBrowserDriver: WebBrowserDriver) extends Matchers with WithClue {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  @Given("""^the motor trader has confirmed the consent of the current keeper$""")
  def the_motor_trader_has_confirmed_the_consent_of_the_current_keeper() = {
    buildDisposeSetup()
    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^the Trader is on the Complete and Confirm page $""")
  def the_Trader_is_on_the_Complete_Confirm_page() = {
    go to BeforeYouStartPage
    click on BeforeYouStartPage.startNow
    pageTitle should equal(SetupTradeDetailsPage.title) withClue trackingId
    pageTitle should equal(SetupTradeDetailsPage.title) withClue trackingId
    SetupTradeDetailsPage.traderName.value = "Big Motors Limited"
    SetupTradeDetailsPage.traderPostcode.value = "qq99qq"
    click on SetupTradeDetailsPage.lookup
    pageTitle shouldEqual  BusinessChooseYourAddressPage.title withClue trackingId
    BusinessChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.selectedAddressLine
    click on BusinessChooseYourAddressPage.select
    VehicleLookupPage.vehicleRegistrationNumber.value = "b1"
    VehicleLookupPage.documentReferenceNumber.value = "11111111111"
    click on VehicleLookupPage.findVehicleDetails
    pageTitle shouldEqual DisposePage.title withClue trackingId
    go to DisposePage
  }

  @Given("""^the motor trader has not confirmed the consent of the current keeper$""")
  def the_motor_trader_has_not_confirmed_the_consent_of_the_current_keeper() = {
    buildDisposeSetup()
    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.lossOfRegistrationConsent
  }

  //test clause - keeper consent error will be displayed on submit
  @Then("""^one error will occur1$""")
  def one_error_will_occur1() = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should contain("consent of the registered keeper") withClue trackingId
  }


  @Given("""^the motor trader has confirmed the consent acknowledgement of the current keeper$""")
  def the_motor_trader_has_confirmed_the_acknowledgement_of_the_current_keeper() = {
    buildDisposeSetup()
    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.consent
  }

  //test clause - loss of registration consent error will be displayed on submit
  @Then("""^one error will occur2$""")
  def one_error_will_occur2() = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should contain("confirm that you have made the keeper aware") withClue trackingId
  }

  @Given("""^that entered details correspond to a valid clean record that has no markers or error codes$""")
  def that_entered_details_correspond_to_a_valid_clean_record_that_has_no_markers_or_error_codes() = {
    go to BeforeYouStartPage
    CookieFactoryForUISpecs.setupTradeDetails().dealerDetails()
    go to VehicleLookupPage
    VehicleLookupPage.vehicleRegistrationNumber.value = "AB12AWR"
    VehicleLookupPage.documentReferenceNumber.value = "11111111112"
    click on VehicleLookupPage.findVehicleDetails
    enterValidDisposalDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  //test case to ensure that page has no markers or error codes i.e. can dispose
  @When("""^they attempt to dispose of the vehicle2$""")
  def they_attempt_to_dispose_of_the_vehicle2() = {
    click on DisposePage.dispose
  }

  @Then("""^dispose success$""")
  def dispose_success() = {
    pageTitle should equal(DisposeSuccessPage.title) withClue trackingId
  }

  @Given("""^that entered details correspond to a valid record which has markers or error codes$""")
  def that_entered_details_correspond_to_a_valid_record_which_has_markers_or_error_codes() = {
    go to BeforeYouStartPage
    CookieFactoryForUISpecs.setupTradeDetails().dealerDetails()
    go to VehicleLookupPage
    VehicleLookupPage.vehicleRegistrationNumber.value = "AB12AWR"
    VehicleLookupPage.documentReferenceNumber.value = "11111111113"
    click on VehicleLookupPage.findVehicleDetails
  }

  @When("""^they attempt to dispose of the vehicle$""")
  def they_attempt_to_dispose_of_the_vehicle() = {
    click on DisposePage.dispose
  }

  @Then("""^three errors will occur$""")
  def three_errors_will_occur() = {
    ErrorPanel.numberOfErrors should equal(3) withClue trackingId
  }

  @When("""^they attempt to dispose of the vehicle and consent is incomplete$""")
  def they_attempt_to_dispose_of_the_vehicle_and_consent_is_incomplete() = {
    click on DisposePage.consent
    click on DisposePage.dispose
  }

  @Then("""^two errors will occur$""")
  def two_errors_will_occur() = {
    ErrorPanel.numberOfErrors should equal(2) withClue trackingId
  }

  @When("""^they give full consent and attempt to dispose of vehicle without disposal date$""")
  def they_give_full_consent_and_attempt_to_dispose_of_vehicle_without_disposal_date() = {
    click on DisposePage.lossOfRegistrationConsent
    click on DisposePage.dispose
  }

  @Then("""^one error will occur$""")
  def one_error_will_occur() = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should contain("Must be a valid date") withClue trackingId
  }

  @When("""^the user enters an invalid disposal date that is too old$""")
  def the_user_enters_an_invalid_disposal_date_that_is_too_old() = {
    enterInvalidDisposalDateTooOld()
    click on DisposePage.dispose
  }

  @Then("""^an error will occur stating "Must be between today and two years ago2"$""")
  def an_error_will_occur_stating_Must_be_between_today_and_two_years_ago2() = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should contain("Must be a valid date") withClue trackingId
  }

  @When("""^the user enters an invalid disposal date bad date format$""")
  def the_user_enters_an_invalid_disposal_date_bad_date_format() = {
    enterInvalidDisposalDateFormat1()
    click on DisposePage.dispose
  }

  @Then("""^an error will occur stating "Must be between today and two years ago3"$""")
  def an_error_will_occur_stating_Must_be_between_today_and_two_years_ago3() = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should contain("Must be a valid date") withClue trackingId
  }

  @When("""^the user enters an invalid disposal date bad month format$""")
  def the_user_enters_an_invalid_disposal_date_bad_month_format() = {
    enterInvalidDisposalDateFormat2()
    click on DisposePage.dispose
  }

  @Then("""^an error will occur stating "Must be between today and two years ago4"$""")
  def an_error_will_occur_stating_Must_be_between_today_and_two_years_ago4() = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should contain("Must be a valid date") withClue trackingId
  }

  @When("""^the user enters an invalid disposal date in future$""")
  def the_user_enters_an_invalid_disposal_date_in_future() = {
    enterInvalidDisposalDateFuture()
    click on DisposePage.dispose
  }

  @Then("""^an error will occur stating "Must be between today and two years ago5"$""")
  def an_error_will_occur_stating_Must_be_between_today_and_two_years_ago5() = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should contain("Must be a valid date") withClue trackingId
  }

  @When("""^the user enters a valid disposal date$""")
  def the_user_enters_a_valid_disposal_date() = {
    enterOldestValidDisposalDate()
  }

  @Then("""^the sell to trade success page is displayed$""")
  def the_sell_to_trade_success_page_is_displayed() = {
    click on dispose
    pageTitle should equal(DisposeSuccessPage.title) withClue trackingId
  }

  private def enterValidDisposalDate() {
    // todays's date
    val today = Calendar.getInstance()
    DisposePage.dateOfDisposalDay.value = f"${today.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${today.get(Calendar.MONTH)}%02d"
    DisposePage.dateOfDisposalYear.value = today.get(Calendar.YEAR).toString
  }

  private def buildDisposeSetup() {
    go to BeforeYouStartPage
    click on BeforeYouStartPage.startNow
    pageTitle should equal(SetupTradeDetailsPage.title) withClue trackingId
    SetupTradeDetailsPage.traderName.value = "Big Motors Limited"
    SetupTradeDetailsPage.traderPostcode.value = "AA99 1AA"
    click on SetupTradeDetailsPage.lookup
    CookieFactoryForUISpecs.setupTradeDetails().
      dealerDetails().
      vehicleAndKeeperDetailsModel().
      vehicleLookupFormModel()
  }

  //return a date that is two years ago
  private def oldestDisposalDate() = {
    val disposalDay = Calendar.getInstance()
    disposalDay.add(Calendar.YEAR, -2)
    disposalDay
  }

  private def enterOldestValidDisposalDate() {
    DisposePage.dateOfDisposalDay.value = f"${oldestDisposalDate().get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${oldestDisposalDate().get(Calendar.MONTH)}%02d"
    DisposePage.dateOfDisposalYear.value = oldestDisposalDate().get(Calendar.YEAR).toString
  }

  private def enterInvalidDisposalDateTooOld() {
    val invlaidDsposalDate = oldestDisposalDate()
    invlaidDsposalDate.add(Calendar.DATE, -1)
    DisposePage.dateOfDisposalDay.value = f"${invlaidDsposalDate.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${invlaidDsposalDate.get(Calendar.MONTH)}%02d"
    DisposePage.dateOfDisposalYear.value = invlaidDsposalDate.get(Calendar.YEAR).toString
  }

  private def enterInvalidDisposalDateFuture() {
    val disposalDate = Calendar.getInstance()
    disposalDate.add(Calendar.DATE, 1)
    DisposePage.dateOfDisposalDay.value = f"${disposalDate.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${disposalDate.get(Calendar.MONTH)}%02d"
    DisposePage.dateOfDisposalYear.value = disposalDate.get(Calendar.YEAR).toString
  }

  // enter single digit date/month
   private def enterInvalidDisposalDateFormat1() {
    // todays's date
    val today = Calendar.getInstance()
    DisposePage.dateOfDisposalDay.value = "1" // single digit, fixed
    DisposePage.dateOfDisposalMonth.value = f"${today.get(Calendar.MONTH)}%02d"
    DisposePage.dateOfDisposalYear.value = today.get(Calendar.YEAR).toString
  }

  // enter single digit date/month
   private def enterInvalidDisposalDateFormat2() {
    // todays's date
    val today = Calendar.getInstance()
    DisposePage.dateOfDisposalDay.value = f"${today.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = "1" // single digit, fixed
    DisposePage.dateOfDisposalYear.value = today.get(Calendar.YEAR).toString
  }

}
