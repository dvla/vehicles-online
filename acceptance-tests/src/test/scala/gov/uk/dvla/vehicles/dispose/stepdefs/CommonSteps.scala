package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import helpers.common.RandomVrmGenerator
import org.scalatest.Matchers
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
import common.helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}

class CommonSteps(webBrowserDriver: WebBrowserDriver) extends WebBrowserDSL with Matchers {

  implicit val cookieFlags = new NoCookieFlags()
  implicit lazy val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
  implicit lazy val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToSetupTradeDetailsPage() = {
    go to BeforeYouStartPage
    page.title should equal(BeforeYouStartPage.title)
    click on BeforeYouStartPage.startNow
    page.title should equal(SetupTradeDetailsPage.title)
  }

  def goToEnterAddressManuallyPage() = {
    goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName enter "Big Motors Limited"
    SetupTradeDetailsPage.traderPostcode enter "AA99 1AA"
    click on SetupTradeDetailsPage.lookup
    page.title should equal(BusinessChooseYourAddressPage.title)
    click on BusinessChooseYourAddressPage.manualAddress
    page.title should equal(EnterAddressManuallyPage.title)
  }

  def goToVehicleLookupPage() = {
    goToEnterAddressManuallyPage()
    page.title should equal(EnterAddressManuallyPage.title)
    EnterAddressManuallyPage.addressBuildingNameOrNumber enter "1 Long Road"
    EnterAddressManuallyPage.addressPostTown enter "Swansea"
    click on EnterAddressManuallyPage.next
    page.title should equal(VehicleLookupPage.title)
  }

  def goToDisposePage() = {
    goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber enter RandomVrmGenerator.vrm
    VehicleLookupPage.documentReferenceNumber enter "11111111111"
    click on VehicleLookupPage.findVehicleDetails
    page.title should equal(DisposePage.title)
  }

  def goToDisposeSuccessPage() = {
    goToDisposePage()
    DisposePage.mileage enter "10000"
    selectTodaysDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
    click on DisposePage.dispose
    page.title should equal(DisposeSuccessPage.title)
  }

  @Given("""^a correctly formatted document reference number "(.*)" has been entered$""")
  def a_correctly_formatted_document_reference_number_has_been_entered(docRefNo:String) = {
    goToVehicleLookupPage()
    // override doc ref no with test value
    VehicleLookupPage.vehicleRegistrationNumber enter RandomVrmGenerator.vrm
    VehicleLookupPage.documentReferenceNumber enter docRefNo
  }

  @Given("""^an incorrectly formatted document reference number "(.*)" has been entered$""")
  def an_incorrectly_formatted_document_reference_number_has_been_entered(docRefNo:String) = {
    a_correctly_formatted_document_reference_number_has_been_entered(docRefNo)
  }

  @Given("""^a correctly formatted vehicle reference mark "(.*)" has been entered$""")
  def a_correctly_formatted_vehicle_reference_mark_has_been_entered(refMark:String) = {
    goToVehicleLookupPage()
    // override doc ref no with test value
    VehicleLookupPage.vehicleRegistrationNumber enter refMark
    VehicleLookupPage.documentReferenceNumber enter "11111111111"
  }

  @Given("""^an incorrectly formatted vehicle reference mark "(.*)" has been entered$""")
  def an_incorrectly_formatted_vehicle_reference_mark_has_been_entered(refMark:String) = {
    a_correctly_formatted_vehicle_reference_mark_has_been_entered(refMark:String)
  }

  @Given("""^details are entered that correspond to a vehicle that has a valid clean record and has no markers or error codes$""")
  def details_are_entered_that_correspond_to_a_vehicle_that_has_a_valid_clean_record_and_has_no_markers_or_error_codes() = {
    goToDisposePage()
    DisposePage.mileage enter "10000"
    selectTodaysDate()
    click on DisposePage.consent
    click on DisposePage.lossOfRegistrationConsent
  }

  private def selectTodaysDate() = {
    click on DisposePage.dateOfDisposalDay
    DisposePage.dateOfDisposalDay enter "25"
    DisposePage.dateOfDisposalMonth enter "11"
    DisposePage.dateOfDisposalYear enter "2014"
  }

  @Given("""^details are entered that correspond to a vehicle that has a valid record but does have markers or error codes$""")
  def details_are_entered_that_correspond_to_a_vehicle_that_has_a_valid_record_but_does_have_markers_or_error_codes() = {
    goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber enter "AA11 AAC"
    VehicleLookupPage.documentReferenceNumber enter "88888888883"
    click on VehicleLookupPage.findVehicleDetails
    page.title should equal(DisposePage.title)
    DisposePage.mileage enter "10000"
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
    page.title should equal(title)
  }

  @Then("""^a single error message "(.*)" is displayed$""")
  def a_single_error_message_msg_is_displayed(message: String) = {
    ErrorPanel.numberOfErrors should equal(1)
    ErrorPanel.text should include(message)
  }

  @Then("""^a message is displayed "(.*?)"$""")
  def a_message_is_displayed(message: String) = {
    page.text should include(message)
  }

  @Then("""^the dispose transaction does not proceed past the "(.*)" step$""")
  def the_dispose_transaction_does_not_proceed_past_the_step(title:String) = {
    page.title should equal(title)
  }
}
