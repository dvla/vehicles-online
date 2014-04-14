package acceptance.disposal_of_vehicle

import org.scalatest.{BeforeAndAfterAll, Matchers, GivenWhenThen, FeatureSpec}
import pages.disposal_of_vehicle._
import helpers.webbrowser._
import services.fakes.FakeDateServiceImpl._
import DisposePage._

class US125_date_of_disposal_business_rules extends FeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with TestHarness {

  private def cacheSetup() = {
    import helpers.disposal_of_vehicle.CacheSetup
    CacheSetup.setupTradeDetails().
      businessChooseYourAddress().
      vehicleDetailsModel().
      vehicleLookupFormModel()
  }

  feature("US125: Date of Disposal - Business Rules") {
    info("As a Motor Trader")
    info("I want to enter the date of disposal of a vehicle")
    info("So that I can complete the transaction")
    info("")

    scenario("No vehicle registration number entered") {
      new WebBrowser {
        Given("the motor trader has entered a valid calendar date")
        And("the date conforms to the business rules")
        cacheSetup()
        go to DisposePage
        dateOfDisposalDay select dateOfDisposalDayValid
        dateOfDisposalMonth select dateOfDisposalMonthValid
        dateOfDisposalYear select dateOfDisposalYearValid
        click on consent
        click on lossOfRegistrationConsent

        When("they submit the details")
        click on dispose

        Then("they are taken to the next step in the transaction")
        page.title should equal("Dispose a vehicle into the motor trade: summary")
      }
    }
  }
}
