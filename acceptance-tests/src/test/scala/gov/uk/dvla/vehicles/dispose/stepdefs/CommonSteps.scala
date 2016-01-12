package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import org.joda.time.LocalDate
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
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

class CommonSteps(webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper {

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

  def selectTodaysDate() = {
    click on DisposePage.dateOfDisposalDay
    val dod = LocalDate.now.minusYears(1)
    DisposePage.dateOfDisposalDay.value = dod.toString("dd")
    DisposePage.dateOfDisposalMonth.value = dod.toString("MM")
    DisposePage.dateOfDisposalYear.value = dod.toString("YYYY")
  }

  @When("""^this is submitted along with any other mandatory information$""")
  def this_is_submitted_along_with_any_other_mandatory_information() = {
    submit()
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
