package helpers.steps_javascript_enabled

import cucumber.api.java.en.{Given, Then, When}
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriverWIthJavaScript}
import org.openqa.selenium.WebDriver
import org.scalatest.concurrent.Eventually.{PatienceConfig, eventually, scaled}
import org.scalatest.Matchers
import org.scalatest.time.{Seconds, Span}
import pages.disposal_of_vehicle.DisposePage.consent
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalDay
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalMonth
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalYear
import pages.disposal_of_vehicle.DisposePage.dispose
import pages.disposal_of_vehicle.DisposePage.lossOfRegistrationConsent
import pages.disposal_of_vehicle.DisposePage.useTodaysDate
import pages.disposal_of_vehicle.{BeforeYouStartPage, DisposePage, DisposeSuccessPage}
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}

final class DisposeSteps(webBrowserDriver:WebBrowserDriverWIthJavaScript) extends WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  @Given("""^the Trader is on the Complete & Confirm page and javascript is enabled for the browser$""")
  def the_Trader_is_on_the_Complete_Confirm_page_and_javascript_is_enabled_for_the_browser() = {
    buildDisposeSetup()

    go to DisposePage
  }

  @When("""^the user checks the 'Use today's date' checkbox for Date of Sale$""")
  def the_user_checks_the_Use_todays_date_checkbox_for_Date_of_Sale() = {
    click on useTodaysDate
  }

  @Then("""^the data of sale will be set to today's date$""")
  def the_date_of_sale_will_be_set_to_todays_date () = {
    dateOfDisposalDay.value should equal(DateOfDisposalDayValid)
    dateOfDisposalMonth.value should equal(DateOfDisposalMonthValid)
    dateOfDisposalYear.value should equal(DateOfDisposalYearValid)
  }

  @Then("""^the user can select 'Confirm sale' without error on the date of sale field$""")
  def the_user_can_select_Confirm_sale_without_error_on_the_date_of_sale_field() = {
    click on consent
    click on lossOfRegistrationConsent
    click on dispose

    // We want to wait for the javascript to execute and redirect to the next page. For build servers we may need to
    // wait longer than the default.
    val timeout: Span = scaled(Span(2, Seconds))
    implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = timeout)

    eventually {page.title should equal(DisposeSuccessPage.title)}
  }

  @When("""^the user manually selects a date using the  Date of Sale date drop downs$""")
  def the_user_manually_selects_a_date_using_the_Date_of_Sale_date_drop_downs() = {
    dateOfDisposalDay select DateOfDisposalDayValid
    dateOfDisposalMonth select DateOfDisposalMonthValid
    dateOfDisposalYear select DateOfDisposalYearValid
  }

  @Then("""^the data of sale will be set to the date selected by the user$""")
  def the_data_of_sale_will_be_set_to_the_date_selected_by_the_user() = {
    dateOfDisposalDay.value should equal(DateOfDisposalDayValid)
    dateOfDisposalMonth.value should equal(DateOfDisposalMonthValid)
    dateOfDisposalYear.value should equal(DateOfDisposalYearValid)
  }

  private def buildDisposeSetup() {
    go to BeforeYouStartPage

    CookieFactoryForUISpecs.setupTradeDetails().
      dealerDetails().
      vehicleDetailsModel().
      vehicleLookupFormModel()
  }
}
