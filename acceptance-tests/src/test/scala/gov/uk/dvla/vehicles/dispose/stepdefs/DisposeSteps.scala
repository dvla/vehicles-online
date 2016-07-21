package gov.uk.dvla.vehicles.dispose.stepdefs

import java.util.Calendar

import cucumber.api.java.en.{Given, Then, When}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, pageSource, pageTitle}
import pages.common.ErrorPanel
import pages.disposal_of_vehicle.{DisposePage, DisposeSuccessPage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class DisposeSteps(webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper {

  val commonSteps = new CommonSteps(webBrowserDriver)

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  @Given("""^the Trader is on the Complete and Confirm page$""")
  def the_Trader_is_on_the_Complete_Confirm_page() = {
    commonSteps.goToDisposePage()
    click on DisposePage.emailInvisible
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
    click on DisposePage.emailInvisible
    click on DisposePage.dispose
  }

  @When("""^the user enters an invalid disposal date that is too old$""")
  def the_user_enters_an_invalid_disposal_date_that_is_too_old() = {
    enterInvalidDisposalDateTooOld()
    click on DisposePage.dispose
  }

  @When("""^the user enters an invalid disposal date bad date format$""")
  def the_user_enters_an_invalid_disposal_date_bad_date_format() = {
    enterInvalidDisposalDateFormat1()
    click on DisposePage.dispose
  }

  @When("""^the user enters an invalid disposal date bad month format$""")
  def the_user_enters_an_invalid_disposal_date_bad_month_format() = {
    enterInvalidDisposalDateFormat2()
    click on DisposePage.dispose
  }

  @When("""^the user enters an invalid disposal date in future$""")
  def the_user_enters_an_invalid_disposal_date_in_future() = {
    enterInvalidDisposalDateFuture()
    click on DisposePage.dispose
  }

  @When("""^the user enters a valid disposal date$""")
  def the_user_enters_a_valid_disposal_date() = {
    enterValidDisposalDate()
    click on DisposePage.dispose
  }

  @Then("""^the sell to trade success page is displayed$""")
  def the_sell_to_trade_success_page_is_displayed() = {
    pageTitle should equal(DisposeSuccessPage.title) withClue trackingId
  }

  @When("""^the user enters a date of sale over 12 months in the past and submits the form$""")
  def the_user_enters_a_date_of_sale_over_twelve_months_in_the_past_and_submits_the_form() = {
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
    enterDisposalDateWarning()
    click on DisposePage.emailInvisible
    click on DisposePage.dispose
  }

  @When("""^the user confirms the date$""")
  def the_user_confirms_the_date() = {
    click on DisposePage.dispose
  }

  @Then("""^the user will remain on the complete and confirm page and a warning will be displayed$""")
  def the_user_will_remain_on_the_complete_and_confirm_page_and_a_warning_will_be_displayed() = {
    pageTitle should equal(DisposePage.title)
    pageSource should include("<div class=\"popup-modal\">")
    pageSource should include("The date you have entered is over 12 months ago")
  }

  private def enterDisposalDateWarning() {
    val invalidDisposalDate = Calendar.getInstance()
    invalidDisposalDate.add(Calendar.MONTH, -13)
    DisposePage.dateOfDisposalDay.value = f"${invalidDisposalDate.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${invalidDisposalDate.get(Calendar.MONTH)+1}%02d"
    DisposePage.dateOfDisposalYear.value = invalidDisposalDate.get(Calendar.YEAR).toString
  }

  private def enterValidDisposalDate() {
    // todays's date
    val today = Calendar.getInstance()
    DisposePage.dateOfDisposalDay.value = f"${today.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${today.get(Calendar.MONTH)+1}%02d"
    DisposePage.dateOfDisposalYear.value = today.get(Calendar.YEAR).toString
  }

  private def disposalDateNoWarning() = {
    val disposalDay = Calendar.getInstance()
    disposalDay.add(Calendar.YEAR, -1)
    disposalDay
  }

  //return a date that is two years ago
  private def oldestDisposalDate() = {
    val disposalDay = Calendar.getInstance()
    disposalDay.add(Calendar.YEAR, -2)
    disposalDay
  }

  private def enterInvalidDisposalDateTooOld() {
    val invalidDisposalDate = oldestDisposalDate()
    invalidDisposalDate.add(Calendar.DATE, -1)
    DisposePage.dateOfDisposalDay.value = f"${invalidDisposalDate.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${invalidDisposalDate.get(Calendar.MONTH)+1}%02d"
    DisposePage.dateOfDisposalYear.value = invalidDisposalDate.get(Calendar.YEAR).toString
  }

  private def enterInvalidDisposalDateFuture() {
    val disposalDate = Calendar.getInstance()
    disposalDate.add(Calendar.DATE, 1)
    DisposePage.dateOfDisposalDay.value = f"${disposalDate.get(Calendar.DATE)}%02d"
    DisposePage.dateOfDisposalMonth.value = f"${disposalDate.get(Calendar.MONTH)+1}%02d"
    DisposePage.dateOfDisposalYear.value = disposalDate.get(Calendar.YEAR).toString
  }

  // enter single digit date/month
   private def enterInvalidDisposalDateFormat1() {
    // todays's date
    val today = Calendar.getInstance()
    DisposePage.dateOfDisposalDay.value = "1" // single digit, fixed
    DisposePage.dateOfDisposalMonth.value = f"${today.get(Calendar.MONTH)+1}%02d"
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
