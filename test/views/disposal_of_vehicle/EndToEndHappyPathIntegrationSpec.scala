package views.disposal_of_vehicle

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import webserviceclients.fakes.FakeAddressLookupService

class EndToEndHappyPathIntegrationSpec extends UiSpec with TestHarness {
  "The happy end to end case" should {
    "follow the happy path trough all the pages" taggedAs UiTag in new WebBrowser {

      info("Going Before You Start page and click start")
      go to BeforeYouStartPage
      click on BeforeYouStartPage.startNow
      page.title should equal(SetupTradeDetailsPage.title)

      info("Enter valid trader details and find the trader address")

      SetupTradeDetailsPage.happyPath()
      page.source.contains(FakeAddressLookupService.PostcodeValid) should equal(true)
      page.title should equal(BusinessChooseYourAddressPage.title)

      info("Select the business address")
      BusinessChooseYourAddressPage.happyPath
      page.title should equal(VehicleLookupPage.title)

      info("enter the vehicle details")
      VehicleLookupPage.happyPath()
      page.title should equal(DisposePage.title)

      info("dispose the vehicle")
      DisposePage.happyPath
      page.title should equal(DisposeSuccessPage.title)
    }
  }
}
