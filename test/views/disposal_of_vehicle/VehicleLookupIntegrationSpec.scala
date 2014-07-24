package views.disposal_of_vehicle

import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.{TestGlobal, TestHarness}
import mappings.disposal_of_vehicle.RelatedCacheKeys
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.common.ErrorPanel
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.EnterAddressManuallyPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import pages.disposal_of_vehicle.VehicleLookupPage.{happyPath, tryLockedVrm, back, exit}
import pages.disposal_of_vehicle.VrmLockedPage
import play.api.test.FakeApplication
import services.fakes.FakeAddressLookupService.addressWithUprn

final class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupPage

      page.title should equal(VehicleLookupPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupPage

      page.source.contains(progressStep(4)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupPage

      page.source.contains(progressStep(4)) should equal(false)
    }

    "Redirect when no traderBusinessName is cached" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when no dealerBusinessName is cached" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(filters.csrf_prevention.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(filters.csrf_prevention.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "findVehicleDetails button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath()

      page.title should equal(DisposePage.title)
    }

    "display one validation error message when no referenceNumber is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(referenceNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when no registrationNumber is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a registrationNumber is entered containing one character" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(registrationNumber = "a")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a registrationNumber is entered containing special characters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(registrationNumber = "$^")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two validation error messages when no vehicle details are entered but consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(referenceNumber = "", registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one validation error message when only a valid registrationNumber is entered and consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    /* TODO Had to comment out because of this error on the build server. Investigate then restore.

      org.openqa.selenium.WebDriverException: Cannot find firefox binary in PATH. Make sure firefox is installed. OS appears to be: LINUX
      [info] Build info: version: '2.42.2', revision: '6a6995d31c7c56c340d6f45a76976d43506cd6cc', time: '2014-06-03 10:52:47'
    [info] System info: host: '***REMOVED***', ip: '***REMOVED***', os.name: 'Linux', os.arch: 'amd64', os.version: '2.6.32-431.el6.x86_64', java.version: '1.7.0_55'
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

    "does not proceed when milage has non-numeric when invalid referenceNumber (Html5Validation enabled)" taggedAs UiTag in new WebBrowser(
        app = fakeAppWithHtml5ValidationEnabledConfig,
        webDriver = WebDriverFactory.webDriver(targetBrowser = "firefox", javascriptEnabled = true)) {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(referenceNumber = "INVALID")

      page.url should equal(VehicleLookupPage.url)
      ErrorPanel.hasErrors should equal(false)
    }*/

    "display one validation error message when invalid referenceNumber (Html5Validation disabled)" taggedAs UiTag in new WebBrowser(app = fakeAppWithHtml5ValidationDisabledConfig) {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(referenceNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "redirect to vrm locked when too many attempting to lookup a locked vrm" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      tryLockedVrm()
      page.title should equal(VrmLockedPage.title)
    }
  }

  "back" should {
    "display previous page when back link is clicked with uprn present" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn)
      go to VehicleLookupPage

      click on back

      page.title should equal(BusinessChooseYourAddressPage.title)
    }

    "display previous page when back link is clicked with no uprn present" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage

      click on back

      page.title should equal(EnterAddressManuallyPage.title)
    }
  }

  "exit button" should {
    "display before you start page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage

      click on exit

      page.title should equal(BeforeYouStartPage.title)
    }

    "remove redundant cookies" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage

      click on exit

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.FullSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails().
      disposeOccurred

  private val fakeAppWithHtml5ValidationEnabledConfig = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("html5Validation.enabled" -> true))

  private val fakeAppWithHtml5ValidationDisabledConfig = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("html5Validation.enabled" -> false))
}