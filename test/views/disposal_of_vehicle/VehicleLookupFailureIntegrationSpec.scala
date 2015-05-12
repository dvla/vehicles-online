package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.VehicleLookupFailurePage.{beforeYouStart, vehicleLookup}
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage, VehicleLookupFailurePage}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts

class VehicleLookupFailureIntegrationSpec extends UiSpec with TestHarness {
  private final val VrmNotFound = "vehicle_and_keeper_lookup_vrm_not_found"
  private final val DocumentReferenceMismatch = "vehicle_and_keeper_lookup_document_reference_mismatch"

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      page.title should equal(VehicleLookupFailurePage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      page.source should not contain ProgressBar.div
    }

    "redirect to setuptrade details if cache is empty on page load" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupFailurePage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect to setuptrade details if only VehicleLookupFormModelCache is populated" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.vehicleLookupFormModel()
      go to VehicleLookupFailurePage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect to setuptrade details if only dealerDetails cache is populated" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.dealerDetails()
      go to VehicleLookupFailurePage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "remove redundant cookies when displayed" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      webDriver.manage().getCookieNamed(VehicleLookupResponseCodeCacheKey) should equal(null)
    }

    "display messages that show that the number of brute force attempts does not impact which messages are displayed when 1 attempt has been made" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel(attempts = 1, maxAttempts = MaxAttempts).
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = VrmNotFound)

      go to VehicleLookupFailurePage
      page.source should include("Only a limited number of attempts can be made to retrieve vehicle details for each vehicle registration number entered.")
    }

    "display messages that show that the number of brute force attempts does not impact which messages are displayed when 2 attempts have been made" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel(attempts = 2, maxAttempts = MaxAttempts).
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = VrmNotFound)

      go to VehicleLookupFailurePage
      page.source should include("Only a limited number of attempts can be made to retrieve vehicle details for each vehicle registration number entered.")
    }

    "display appropriate messages for document reference mismatch" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel().
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = DocumentReferenceMismatch)

      go to VehicleLookupFailurePage
      page.source should include("Only a limited number of attempts can be made to retrieve vehicle details for each vehicle registration number entered.")
    }

  }

  "vehicleLookup button" should {
    "redirect to vehiclelookup when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      click on vehicleLookup
      page.title should equal(VehicleLookupPage.title)
    }
  }

  "beforeYouStart button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      click on beforeYouStart
      page.title should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      bruteForcePreventionViewModel().
      vehicleLookupFormModel().
      vehicleLookupResponseCode()
}
