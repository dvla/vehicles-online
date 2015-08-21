package views.disposal_of_vehicle

import composition.{TestGlobal, TestHarness}
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import models.AllCacheKeys
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import pages.common.ErrorPanel
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.EnterAddressManuallyPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import pages.disposal_of_vehicle.VehicleLookupPage.{happyPath, tryLockedVrm, back, exit}
import pages.disposal_of_vehicle.VrmLockedPage
import ProgressBar.progressStep
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication
import webserviceclients.fakes.FakeAddressLookupService.addressWithUprn

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
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "display the v5c image on the page with Javascript disabled" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupPage
      page.title should equal(VehicleLookupPage.title)

      new WebDriverWait(webDriver, 3).until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//div[@data-tooltip='tooltip_documentReferenceNumber']"))
      )
    }

    "put the v5c image in a tooltip with Javascript enabled" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupPage
      val v5c = By.xpath("//div[@data-tooltip='tooltip_documentReferenceNumber']")
      val waiting = new WebDriverWait(webDriver, 3)
      waiting.until(ExpectedConditions.presenceOfElementLocated(v5c))
      waiting.until(ExpectedConditions.invisibilityOfElementLocated(v5c))
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

    "display one validation error message when a registrationNumber " +
      "is entered containing one character" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(registrationNumber = "a")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a registrationNumber is " +
      "entered containing special characters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(registrationNumber = "$^")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two validation error messages when no vehicle details are " +
      "entered but consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(referenceNumber = "", registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one validation error message when only a valid registrationNumber is " +
      "entered and consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    /* TODO Had to comment out because of this error on the build server. Investigate then restore.

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

    "does not proceed when milage has non-numeric when invalid referenceNumber (Html5Validation enabled)" taggedAs UiTag in new WebBrowser(
        app = fakeAppWithHtml5ValidationEnabledConfig,
        webDriver = WebDriverFactory.webDriver(targetBrowser = "firefox", javascriptEnabled = true)) {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(referenceNumber = "INVALID")

      page.url should equal(VehicleLookupPage.url)
      ErrorPanel.hasErrors should equal(false)
    }*/

    "display one validation error message when invalid referenceNumber " +
      "(Html5Validation disabled)" taggedAs UiTag in new WebBrowser(app = fakeAppWithHtml5ValidationDisabledConfig) {
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
    "display BusinessChooseYourAddress page when back link is " +
      "clicked with uprn present" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn)
      go to VehicleLookupPage

      click on back

      page.title should equal(BusinessChooseYourAddressPage.title)
    }

    "display EnterAddressManually page when back link is clicked " +
      "after user has manually entered the address" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn).
        enterAddressManually()
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

    "remove redundant cookies" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage

      click on exit

      // Verify the cookies identified by the full set of cache keys have been removed
      AllCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails().
      disposeOccurred

  private val fakeAppWithHtml5ValidationDisabledConfig =
    LightFakeApplication(TestGlobal, Map("html5Validation.enabled" -> false))
}
