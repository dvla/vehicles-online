package helpers.steps

import pages.disposal_of_vehicle.VehicleLookupPage
import helpers.webbrowser.WebBrowserDriver
import pages.common.ErrorPanel
import cucumber.api.java.en.{Given, Then, When}
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.webbrowser.WebBrowserDSL
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.disposal_of_vehicle.DisposePage.consent
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalDay
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalMonth
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalYear
import pages.disposal_of_vehicle.DisposePage.dispose
import pages.disposal_of_vehicle.DisposePage.lossOfRegistrationConsent
import pages.disposal_of_vehicle.DisposePage.useTodaysDate
import pages.disposal_of_vehicle.{BeforeYouStartPage, DisposePage, DisposeSuccessPage}
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}

final class DisposeSteps(webBrowserDriver:WebBrowserDriver) extends WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  @Given("""^the motor trader has confirmed the consent of the current keeper$""")
  def the_motor_trader_has_confirmed_the_consent_of_the_current_keeper() = {
    buildDisposeSetup()

    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^the Trader is on the Complete & Confirm page and javascript is not enabled for the browser$""")
  def the_Trader_is_on_the_Complete_Confirm_page_and_javascript_is_not_enabled_for_the_browser() = {
    buildDisposeSetup()

    go to DisposePage
  }

  @Given("""^the motor trader has not confirmed the consent of the current keeper$""")
  def the_motor_trader_has_not_confirmed_the_consent_of_the_current_keeper() = {
    buildDisposeSetup()

    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^the motor trader has confirmed the acknowledgement of the current keeper$""")
  def the_motor_trader_has_confirmed_the_acknowledgement_of_the_current_keeper() = {
    buildDisposeSetup()

    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^the motor trader has not confirmed the acknowledgement of the current keeper$""")
  def the_motor_trader_has_not_confirmed_the_acknowledgement_of_the_current_keeper() = {
    buildDisposeSetup()

    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.consent
  }

  @Given("""^the motor trader has entered a valid calendar date which conforms to business rules$""")
  def the_motor_trader_has_entered_a_valid_calendar_date() = {
    buildDisposeSetup()

    go to DisposePage
    enterValidDisposalDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^the motor trader has entered a valid calendar date which does not conform to business rules$""")
  def the_motor_trader_has_entered_a_valid_calendar_date_which_does_not_conform_to_the_business_rules() = {
    buildDisposeSetup()

    go to DisposePage
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^that entered details correspond to a valid clean record that has no markers or error codes$""")
  def that_entered_details_correspond_to_a_valid_clean_record_that_has_no_markers_or_error_codes() = {
    go to BeforeYouStartPage
    CookieFactoryForUISpecs.setupTradeDetails().
      dealerDetails()

    go to VehicleLookupPage
    VehicleLookupPage.vehicleRegistrationNumber enter "AB12AWR"
    VehicleLookupPage.documentReferenceNumber enter "11111111112"
    click on VehicleLookupPage.findVehicleDetails
    enterValidDisposalDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @Given("""^that entered details correspond to a valid record which has markers or error codes$""")
  def that_entered_details_correspond_to_a_valid_record_which_has_markers_or_error_codes() = {
    go to BeforeYouStartPage
    CookieFactoryForUISpecs.setupTradeDetails().
      dealerDetails()

    go to VehicleLookupPage
    VehicleLookupPage.vehicleRegistrationNumber enter "AB12AWR"
    VehicleLookupPage.documentReferenceNumber enter "11111111113"
    click on VehicleLookupPage.findVehicleDetails
    enterValidDisposalDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @When("""^they attempt to dispose of the vehicle$""")
  def they_attempt_to_dispose_of_the_vehicle() = {
    click on DisposePage.dispose
  }

  @When("""^the user manually selects a date using the  Date of Sale date drop downs$""")
  def the_user_manually_selects_a_date_using_the_Date_of_Sale_date_drop_downs() = {
    enterValidDisposalDate()
  }

  @When("""^the Trader tries to select "Confirm Sale" without setting the Date of Sale$""")
  def the_Trader_tries_to_select_Confirm_Sale_without_setting_the_Date_of_Sale() = {
    click on consent
    click on lossOfRegistrationConsent
    click on dispose
  }

  @When("""^the user checks the 'Use today's date' checkbox for Date of Sale$""")
  def the_user_checks_the_Use_todays_date_checkbox_for_Date_of_Sale() = {
    click on useTodaysDate
  }

  @Then("""^the date dropdown is still unset$""")
  def the_date_dropdown_is_still_unset() = {
    dateOfDisposalDay.value should equal("")
    dateOfDisposalMonth.value should equal("")
    dateOfDisposalYear.value should equal("")
  }

  @Then("""^the user can select "Confirm sale" without error on the date of sale field$""")
  def the_user_can_select_Confirm_sale_without_error_on_the_date_of_sale_field() = {
    click on consent
    click on lossOfRegistrationConsent
    click on dispose

    page.title should equal(DisposeSuccessPage.title)
  }

  @Then("""^an error will occur stating "Must be between today and two years ago"$""")
  def an_error_will_occur_stating_Must_be_between_today_and_two_years_ago() = {
    ErrorPanel.numberOfErrors should equal(1)
  }

  private def enterValidDisposalDate() {
    DisposePage.dateOfDisposalDay select DateOfDisposalDayValid
    DisposePage.dateOfDisposalMonth select DateOfDisposalMonthValid
    DisposePage.dateOfDisposalYear select DateOfDisposalYearValid
  }

  private def buildDisposeSetup() {
    go to BeforeYouStartPage

    CookieFactoryForUISpecs.setupTradeDetails().
      dealerDetails().
      vehicleDetailsModel().
      vehicleLookupFormModel()
  }
}
