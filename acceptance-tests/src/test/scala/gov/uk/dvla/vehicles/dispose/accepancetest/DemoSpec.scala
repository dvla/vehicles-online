package gov.uk.dvla.vehicles.dispose.accepancetest

import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen, Matchers}
import helpers.webbrowser.{WebDriverFactory, WebBrowserDSL, TestHarness}
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.VehicleLookupPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.BeforeYouStartPage
import webserviceclients.fakes.FakeAddressLookupService.{TraderBusinessNameValid, PostcodeValidWithSpace}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageValid
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}

final class DemoSpec extends FeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with TestHarness {
  implicit val webDriver = WebDriverFactory.webDriver

  feature("Dispose of a vehicle to trade") {
    info("As a vehicle trader")
    info("I want to dispose of a vehicle for a customer")
    info("So they will be removed from the vehicle record as current keeper")
    info("")

    scenario("Sell a vehicle to the trade: happy path") {

      new WebBrowserDSL {

        Given("I am on the vehicles online prototype site")
        go to BeforeYouStartPage

        And("I click the Start now button to begin the transaction")
        click on BeforeYouStartPage.startNow

        And("I enter \"Car Giant\" in the business name field")
        SetupTradeDetailsPage.traderName enter TraderBusinessNameValid

        And("I enter \"CM8 1QJ\" in the business postcode field")
        SetupTradeDetailsPage.traderPostcode enter PostcodeValidWithSpace

        And("I click the Look-up button")
        click on SetupTradeDetailsPage.lookup

        And("I select \"1, OLIVERS DRIVE, WITHAM, CM8 1QJ\"")
        BusinessChooseYourAddressPage.chooseAddress.value = traderUprnValid.toString

        And("I click the Select button")
        click on BusinessChooseYourAddressPage.select

        And("I enter \"A1\" in the vehicle registration number field")
        VehicleLookupPage.vehicleRegistrationNumber enter RegistrationNumberValid

        And("I enter \"12345678910\" in the V5C document reference number field")
        VehicleLookupPage.documentReferenceNumber enter ReferenceNumberValid

        And("I click the find vehicle details button")
        click on VehicleLookupPage.findVehicleDetails

        And("I enter \"10000\" in the vehicle mileage field")
        DisposePage.mileage enter MileageValid

        And("I select \"01\" from the date of disposal day dropdown")
        DisposePage.dateOfDisposalDay select DateOfDisposalDayValid

        And("I select \"March\" from the date of disposal month dropdown")
        DisposePage.dateOfDisposalMonth select DateOfDisposalMonthValid

        And("I enter \"2014\" in the date of disposal year field")
        DisposePage.dateOfDisposalYear select DateOfDisposalYearValid

        And("I select \"I have the consent of the current keeper to dispose of this vehicle\"")
        click on DisposePage.consent

        And("I select \"I have the confirmation that the current keeper is aware that the registration will be disposed of with the vehicle\"")
        click on DisposePage.lossOfRegistrationConsent

        When("I click the dispose button")
        click on DisposePage.dispose

        Then("I should see \"Summary \"")
        page.text should include("Summary")
      }
    }
  }
}
