package views.disposal_of_vehicle

import composition.TestHarness
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import org.scalatest.selenium.WebBrowser
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import webserviceclients.fakes.FakeAddressLookupService

class EndToEndHappyPathIntegrationSpec extends UiSpec with TestHarness {
  "The happy end to end case" should {
    "follow the happy path through all the pages" taggedAs UiTag in new WebBrowserForSelenium {

      info("Going Before You Start page and click start")
      go to BeforeYouStartPage
      click on BeforeYouStartPage.startNow
      pageTitle should equal(SetupTradeDetailsPage.title)

      info("Enter valid trader details and find the trader address")

      SetupTradeDetailsPage.happyPath(traderBusinessEmail = None)
      pageSource.contains(FakeAddressLookupService.PostcodeValid) should equal(true)
      pageTitle should equal(BusinessChooseYourAddressPage.title)

      info("Select the business address")
      BusinessChooseYourAddressPage.happyPath
      pageTitle should equal(VehicleLookupPage.title)

      info("enter the vehicle details")
      VehicleLookupPage.happyPath()
      pageTitle should equal(DisposePage.title)

      info("dispose the vehicle")
      DisposePage.happyPath
      pageTitle should equal(DisposeSuccessPage.title)
    }
  }
}
