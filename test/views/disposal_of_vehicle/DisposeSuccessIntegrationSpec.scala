package views.disposal_of_vehicle

import helpers.UiSpec
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.DisposeSuccessPage._
import pages.disposal_of_vehicle._
import mappings.disposal_of_vehicle.RelatedCacheKeys

final class DisposeSuccessIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeSuccessPage

      page.title should equal(DisposeSuccessPage.title)
    }
    "redirect when no details are cached" in new WebBrowser {
      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DealerDetails are cached" in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.dealerDetailsIntegration()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only VehicleDetails are cached" in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.vehicleDetailsModelIntegration()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DisposeDetails are cached" in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.disposeFormModelIntegration()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DealerDetails and VehicleDetails are cached" in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        dealerDetailsIntegration().
        vehicleDetailsModelIntegration()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DisposeDetails and VehicleDetails are cached" in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        disposeFormModelIntegration().
        vehicleDetailsModelIntegration()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DisposeDetails and DealerDetails are cached" in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        dealerDetailsIntegration().
        disposeFormModelIntegration()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }
  }

  "newDisposal button" should {
    "display vehicle lookup page" in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      DisposeSuccessPage.happyPath

      page.title should equal(VehicleLookupPage.title)
    }

    "remove redundant cookies" in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage

      click on newDisposal

      // Verify the cookies identified by the dispose set of cache keys have been removed
      RelatedCacheKeys.DisposeSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })

      // Verify the cookies identified by the trade details set of cache keys are present
      RelatedCacheKeys.TradeDetailsSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should not equal null
      })
    }
  }

  "exit button" should {
    "remove redundant cookies" in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage

      click on exitDisposal

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.FullSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetailsIntegration().
      businessChooseYourAddressIntegration().
      dealerDetailsIntegration().
      vehicleDetailsModelIntegration().
      disposeFormModelIntegration().
      disposeTransactionIdIntegration().
      vehicleRegistrationNumberIntegration()
}
