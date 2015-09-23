package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser.pageTitle
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.submit
import org.openqa.selenium.WebDriver
import pages.common.ErrorPanel
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.EnterAddressManuallyPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.clientsidesession.NoCookieFlags
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WithClue, WebBrowserDriver}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

class CommonSteps(webBrowserDriver: WebBrowserDriver) extends Matchers with WithClue {

  implicit val cookieFlags = new NoCookieFlags()
  implicit lazy val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
  implicit lazy val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToSetupTradeDetailsPage() = {
    go to BeforeYouStartPage
    pageTitle should equal(BeforeYouStartPage.title) withClue trackingId
    click on BeforeYouStartPage.startNow
    pageTitle should equal(SetupTradeDetailsPage.title) withClue trackingId
  }

  def goToEnterAddressManuallyPage() = {
    goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName.value = "Big Motors Limited"
    SetupTradeDetailsPage.traderPostcode.value = "AA99 1AA"
    click on SetupTradeDetailsPage.lookup
    pageTitle should equal(BusinessChooseYourAddressPage.title) withClue trackingId
    click on BusinessChooseYourAddressPage.manualAddress
    pageTitle should equal(EnterAddressManuallyPage.title) withClue trackingId
  }

  def goToVehicleLookupPage() = {
    goToEnterAddressManuallyPage()
    pageTitle should equal(EnterAddressManuallyPage.title) withClue trackingId
    EnterAddressManuallyPage.addressBuildingNameOrNumber.value = "1 Long Road"
    EnterAddressManuallyPage.addressPostTown.value = "Swansea"
    click on EnterAddressManuallyPage.next
    pageTitle should equal(VehicleLookupPage.title) withClue trackingId
  }

  def goToDisposePage() = {
    goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber.value = RandomVrmGenerator.uniqueVrm
    VehicleLookupPage.documentReferenceNumber.value = "11111111111"
    click on VehicleLookupPage.findVehicleDetails
    pageTitle should equal(DisposePage.title) withClue trackingId
  }

  def goToDisposeSuccessPage() = {
    goToDisposePage()
    DisposePage.mileage.value = "10000"
    selectTodaysDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
    click on DisposePage.dispose
    pageTitle should equal(DisposeSuccessPage.title) withClue trackingId
  }

  @Given("""^a correctly formatted document reference number "(.*)" has been entered$""")
  def a_correctly_formatted_document_reference_number_has_been_entered(docRefNo:String) = {
    goToVehicleLookupPage()
    // override doc ref no with test value
    VehicleLookupPage.vehicleRegistrationNumber.value = RandomVrmGenerator.uniqueVrm
    VehicleLookupPage.documentReferenceNumber.value = docRefNo
  }

  @Given("""^an incorrectly formatted document reference number "(.*)" has been entered$""")
  def an_incorrectly_formatted_document_reference_number_has_been_entered(docRefNo:String) = {
    a_correctly_formatted_document_reference_number_has_been_entered(docRefNo)
  }

  @Given("""^a correctly formatted vehicle reference mark "(.*)" has been entered$""")
  def a_correctly_formatted_vehicle_reference_mark_has_been_entered(refMark:String) = {
    goToVehicleLookupPage()
    // override doc ref no with test value
    VehicleLookupPage.vehicleRegistrationNumber.value = refMark
    VehicleLookupPage.documentReferenceNumber.value = "11111111111"
  }

  @Given("""^an incorrectly formatted vehicle reference mark "(.*)" has been entered$""")
  def an_incorrectly_formatted_vehicle_reference_mark_has_been_entered(refMark:String) = {
    a_correctly_formatted_vehicle_reference_mark_has_been_entered(refMark:String)
  }

  @Given("""^details are entered that correspond to a vehicle that has a valid clean record and has no markers or error codes$""")
  def details_are_entered_that_correspond_to_a_vehicle_that_has_a_valid_clean_record_and_has_no_markers_or_error_codes() = {
    goToDisposePage()
    DisposePage.mileage.value = "10000"
    selectTodaysDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  private def selectTodaysDate() = {
    click on DisposePage.dateOfDisposalDay
    DisposePage.dateOfDisposalDay.value = "25"
    DisposePage.dateOfDisposalMonth.value = "11"
    DisposePage.dateOfDisposalYear.value = "2014"
  }

  @Given("""^details are entered that correspond to a vehicle that has a valid record but does have markers or error codes$""")
  def details_are_entered_that_correspond_to_a_vehicle_that_has_a_valid_record_but_does_have_markers_or_error_codes() = {
    goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber.value = "AA11 AAC"
    VehicleLookupPage.documentReferenceNumber.value = "88888888883"
    click on VehicleLookupPage.findVehicleDetails
    pageTitle should equal(DisposePage.title) withClue trackingId
    DisposePage.mileage.value = "10000"
    selectTodaysDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  @When("""^this is submitted along with any other mandatory information$""")
  def this_is_submitted_along_with_any_other_mandatory_information() = {
    submit()
  }

  @Then("""^the document reference number "(.*)" is retained$""")
  def the_document_reference_number_is_retained(docRefNo:String) = {
    /*def unquoteString(quotedString: String): String = {
      val stringWithoutEnclosingQuotes = quotedString.drop(1).dropRight(1)
      stringWithoutEnclosingQuotes.replace("\\\"", "\"")
    }

    val webDriverCookies = webDriver.manage().getCookies.toSet
    val playCookies = webDriverCookies.map(c => Cookie(c.getName, unquoteString(c.getValue)))

    playCookies.getModel[VehicleLookupFormModel] match {
      case Some(VehicleLookupFormModel(referenceNumber, _)) =>
        referenceNumber should equal(docRefNo)
      case None =>
        assert(false, "No valid cookie exists for this model")
    }*/
  }

  @Then("""^the vehicle reference mark "(.*)" is retained$""")
  def the_vehicle_reference_mark_is_retained(refMark:String) = {
  }

  @Then("""^the next step in the dispose transaction "(.*)" is shown$""")
  def the_next_step_in_the_dispose_transaction_is_shown(title:String) = {
    pageTitle should equal(title) withClue trackingId
  }

  @Then("""^a single error message "(.*)" is displayed$""")
  def a_single_error_message_msg_is_displayed(message: String) = {
    ErrorPanel.numberOfErrors should equal(1) withClue trackingId
    ErrorPanel.text should include(message) withClue trackingId
  }

  @Then("""^a message is displayed "(.*?)"$""")
  def a_message_is_displayed(message: String) = {
    pageSource should include(message) withClue trackingId
  }

  @Then("""^the dispose transaction does not proceed past the "(.*)" step$""")
  def the_dispose_transaction_does_not_proceed_past_the_step(title:String) = {
    pageTitle should equal(title) withClue trackingId
  }
}
