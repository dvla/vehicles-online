package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar.progressStep
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import models.DisposeFormModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey}
import models.{AllCacheKeys, DisposeCacheKeys, TradeDetailsCacheKeys}
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.disposal_of_vehicle.DisposeSuccessPage.{exitDisposal, newDisposal}
import pages.disposal_of_vehicle.{BeforeYouStartPage, DisposeSuccessPage, SetupTradeDetailsPage, VehicleLookupPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction

final class DisposeSuccessIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeSuccessPage

      page.title should equal(DisposeSuccessPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeSuccessPage

      page.source.contains(progressStep(6)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeSuccessPage

      page.source.contains(progressStep(6)) should equal(false)
    }

    "redirect when no details are cached" taggedAs UiTag in new WebBrowser {
      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DealerDetails are cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.dealerDetails()

      go to DisposeSuccessPage

      page.title should equal(VehicleLookupPage.title)
    }

    "redirect when only VehicleDetails are cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.vehicleDetailsModel()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DisposeDetails are cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.disposeFormModel()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DealerDetails and VehicleDetails are cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        dealerDetails().
        vehicleDetailsModel()

      go to DisposeSuccessPage

      page.title should equal(VehicleLookupPage.title)
    }

    "redirect when only DisposeDetails and VehicleDetails are cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        disposeFormModel().
        vehicleDetailsModel()

      go to DisposeSuccessPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when only DisposeDetails and DealerDetails are cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        dealerDetails().
        disposeFormModel()

      go to DisposeSuccessPage

      page.title should equal(VehicleLookupPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeSuccessPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "newDisposal button" should {
    "display vehicle lookup page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      DisposeSuccessPage.happyPath

      page.title should equal(VehicleLookupPage.title)
    }

    "remove and retain cookies" taggedAs UiTag in new WebBrowser {
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
  }

  "exit button" should {
    "display before you start page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage

      click on exitDisposal

      page.title should equal(BeforeYouStartPage.title)
    }

    "remove redundant cookies" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeSuccessPage

      click on exitDisposal

      // Verify the cookies identified by the full set of cache keys have been removed
      AllCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    /*
    TODO Had to comment out because of this error on the build server. Investigate then restore.

      org.openqa.selenium.WebDriverException: Cannot find firefox binary in PATH. Make sure firefox is installed. OS appears to be: LINUX
[info] Build info: version: '2.42.2', revision: '6a6995d31c7c56c340d6f45a76976d43506cd6cc', time: '2014-06-03 10:52:47'
[info] Driver info: driver.version: FirefoxDriver
[info]     at org.openqa.selenium.firefox.internal.Executable.<init>(Executable.java:72)
[info]     at org.openqa.selenium.firefox.FirefoxBinary.<init>(FirefoxBinary.java:59)
[info]     at org.openqa.selenium.firefox.FirefoxBinary.<init>(FirefoxBinary.java:55)
[info]     at org.openqa.selenium.firefox.FirefoxDriver.<init>(FirefoxDriver.java:99)
[info]     at helpers.webbrowser.WebDriverFactory$.firefoxDriver(WebDriverFactory.scala:75)
[info]     at helpers.webbrowser.WebDriverFactory$.webDriver(WebDriverFactory.scala:34)
[info]     at views.disposal_of_vehicle.DisposeSuccessIntegrationSpec$$anonfun$3$$anonfun$apply$mcV$sp$16$$anonfun$apply$mcV$sp$17$$anon$16.<init>(DisposeSuccessIntegrationSpec.scala:180)
[info]     at views.disposal_of_vehicle.DisposeSuccessIntegrationSpec$$anonfun$3$$anonfun$apply$mcV$sp$16$$anonfun$apply$mcV$sp$17.apply$mcV$sp(DisposeSuccessIntegrationSpec.scala:180)
[info]     at views.disposal_of_vehicle.DisposeSuccessIntegrationSpec$$anonfun$3$$anonfun$apply$mcV$sp$16$$anonfun$apply$mcV$sp$17.apply(DisposeSuccessIntegrationSpec.scala:180)
[info]     at views.disposal_of_vehicle.DisposeSuccessIntegrationSpec$$anonfun$3$$anonfun$apply$mcV$sp$16$$anonfun$apply$mcV$sp$17.apply(DisposeSuccessIntegrationSpec.scala:180)
[info]     ...

    "browser back button" should {
      "display VehicleLookup page when in firefox with javascript enabled" taggedAs UiTag in new WebBrowser(webDriver = WebDriverFactory.webDriver(targetBrowser = "firefox", javascriptEnabled = true)) {
        go to BeforeYouStartPage
        CookieFactoryForUISpecs.
          dealerDetails().
          vehicleLookupFormModel().
          vehicleDetailsModel()
        DisposePage.happyPath

        webDriver.navigate().back()

        page.title should equal(VehicleLookupPage.title)
      }
    }*/
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      businessChooseYourAddress().
      enterAddressManually().
      dealerDetails().
      vehicleDetailsModel().
      disposeFormModel().
      disposeTransactionId().
      vehicleRegistrationNumber().
      disposeFormTimestamp()
}
