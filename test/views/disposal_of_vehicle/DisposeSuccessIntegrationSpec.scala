package views.disposal_of_vehicle


import composition.TestHarness
import helpers.common.ProgressBar.progressStep
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import models.DisposeFormModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey}
import models.{AllCacheKeys, DisposeCacheKeys, TradeDetailsCacheKeys}
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser
import WebBrowser.enter
import WebBrowser.Checkbox
import WebBrowser.checkbox
import WebBrowser.TextField
import WebBrowser.textField
import WebBrowser.TelField
import WebBrowser.telField
import WebBrowser.RadioButton
import WebBrowser.radioButton
import WebBrowser.click
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.DisposeSuccessPage.{exitDisposal, newDisposal}
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import views.disposal_of_vehicle.DisposeSuccess.NewDisposalId
import pages.disposal_of_vehicle.DisposePage

final class DisposeSuccessIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage
      pageTitle should equal(DisposeSuccessPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage
      pageSource.contains(progressStep(6)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage
      pageSource.contains(progressStep(6)) should equal(false)
    }

    "redirect when no details are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to DisposeSuccessPage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DealerDetails are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.dealerDetails()
      go to DisposeSuccessPage
      pageTitle should equal(VehicleLookupPage.title)
    }

    "redirect when only VehicleDetails are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.vehicleAndKeeperDetailsModel()
      go to DisposeSuccessPage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DisposeDetails are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.disposeFormModel()
      go to DisposeSuccessPage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DealerDetails and VehicleDetails are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        dealerDetails().
        vehicleAndKeeperDetailsModel()

      go to DisposeSuccessPage
      pageTitle should equal(VehicleLookupPage.title)
    }

    "redirect when only DisposeDetails and VehicleDetails are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        disposeFormModel().
        vehicleAndKeeperDetailsModel()

      go to DisposeSuccessPage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DisposeDetails and DealerDetails are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        dealerDetails().
        disposeFormModel()

      go to DisposeSuccessPage
      pageTitle should equal(VehicleLookupPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeSuccessPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }
  }

  "newDisposal button" should {
    "display vehicle lookup page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      DisposeSuccessPage.happyPath
      pageTitle should equal(VehicleLookupPage.title)
    }

    "remove and retain cookies" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage
      click on newDisposal
      // Verify the cookies identified by the dispose set of cache keys have been removed
      DisposeCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })

      // Verify the cookies identified by the trade details set of cache keys are present.
      TradeDetailsCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should not equal null
      })

      // Verify that the back button prevention cookie is present
      webDriver.manage().getCookieNamed(PreventGoingToDisposePageCacheKey) should not equal null

      // Verify that the dispose occurred is present
      webDriver.manage().getCookieNamed(DisposeOccurredCacheKey) should not equal null
    }

    "be present when the disposal is done by the trade" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage
      pageTitle should equal(DisposeSuccessPage.title)
      pageSource should include(s"""id="$NewDisposalId"""")
    }
  }

  "exit button" should {
    "display before you start page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage
      click on exitDisposal
      pageTitle should equal(BeforeYouStartPage.title)
    }

    "remove redundant cookies" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage
      click on exitDisposal
      // Verify the cookies identified by the full set of cache keys have been removed
      AllCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "remove redundant cookies with ceg identifier" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup().withIdentifier("ceg")
      go to DisposeSuccessPage
      click on exitDisposal
      // Verify the cookies identified by the full set of cache keys have been removed
      AllCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "browser back button" should {
      "display VehicleLookup page with javascript enabled" taggedAs UiTag in new PhantomJsByDefault {
        go to BeforeYouStartPage
        CookieFactoryForUISpecs.
          dealerDetails().
          vehicleLookupFormModel().
          vehicleAndKeeperDetailsModel()
        DisposePage.happyPath
        webDriver.navigate().back()

        pageTitle should equal(VehicleLookupPage.title)
      }
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      businessChooseYourAddress().
      enterAddressManually().
      dealerDetails().
      vehicleAndKeeperDetailsModel().
      disposeFormModel().
      disposeTransactionId().
      vehicleRegistrationNumber().
      disposeFormTimestamp()
}
