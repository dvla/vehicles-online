package views.disposal_of_vehicle

import composition.{TestGlobal, TestHarness}
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import models.DisposeFormModelBase.Form.TodaysDateOfDisposal
import models.PrivateDisposeFormModel.Form.EmailOptionId
import org.openqa.selenium.{By, WebDriver}
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
import org.scalatest.concurrent.Eventually.{eventually, PatienceConfig, scaled}
import org.scalatest.time.{Seconds, Span}
import pages.common.ErrorPanel
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DisposePage.back
import pages.disposal_of_vehicle.DisposePage.consent
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalDay
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalMonth
import pages.disposal_of_vehicle.DisposePage.dateOfDisposalYear
import pages.disposal_of_vehicle.DisposePage.dispose
import pages.disposal_of_vehicle.DisposePage.happyPath
import pages.disposal_of_vehicle.DisposePage.lossOfRegistrationConsent
import pages.disposal_of_vehicle.DisposePage.mileage
import pages.disposal_of_vehicle.DisposePage.sadPath
import pages.disposal_of_vehicle.DisposePage.title
import pages.disposal_of_vehicle.DisposePage.useTodaysDate
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import ProgressBar.progressStep
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageInvalid

final class DisposeIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposePage

      pageTitle should equal(title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposePage

      pageSource.contains(progressStep(5)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposePage

      pageSource.contains(progressStep(5)) should equal(false)
    }

    "redirect when no vehicleDetailsModel is cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.dealerDetails()

      go to DisposePage

      pageTitle should equal(VehicleLookupPage.title)
    }

    "redirect when no businessChooseYourAddress is cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.vehicleAndKeeperDetailsModel()

      go to DisposePage

      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "redirect when no traderBusinessName is cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to DisposePage

      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposePage
      val csrf = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(
        uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName
      )
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "not display optional email" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposePage
      webDriver.findElements(By.id(EmailOptionId)).isEmpty should be (true)
    }
  }

  "dispose button" should {
    "display DisposeSuccess page on correct submission" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().
        vehicleLookupFormModel()

      happyPath

      pageTitle should equal(DisposeSuccessPage.title)
    }

    // This test needs to run with javaScript enabled.
    "display DisposeSuccess page on correct submission with " +
      "javascript enabled" taggedAs UiTag ignore new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup().vehicleLookupFormModel()

      happyPath

      // We want to wait for the javascript to execute and redirect to the next page. For build servers we may need to
      // wait longer than the default.
      val timeout: Span = scaled(Span(2, Seconds))
      implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = timeout)

      eventually {pageTitle should equal(DisposeSuccessPage.title)}
    }

    // This test needs to run with javaScript enabled.
    "display DisposeSuccess page on correct submission when a user auto populates " +
      "the date of disposal with javascript enabled" taggedAs UiTag ignore new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup().vehicleLookupFormModel()
      go to DisposePage

      click on useTodaysDate

      dateOfDisposalDay.value should equal(DateOfDisposalDayValid)
      dateOfDisposalMonth.value should equal(DateOfDisposalMonthValid)
      dateOfDisposalYear.value should equal(DateOfDisposalYearValid)

      click on consent
      click on lossOfRegistrationConsent

      dispose.underlying.getAttribute("class") should not include "disabled"

      click on dispose

      // We want to wait for the javascript to execute and redirect to the next page. For build servers we may need to
      // wait longer than the default.
      val timeout: Span = scaled(Span(2, Seconds))
      implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = timeout)

      eventually(dispose.underlying.getAttribute("class").contains("disabled"))
      eventually {pageTitle should equal(DisposeSuccessPage.title)}
    }

    "display validation errors when no data is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      sadPath

      ErrorPanel.numberOfErrors should equal(3)
    }

    "display validation errors when month and year are input but no day" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage
      dateOfDisposalMonth.value = DateOfDisposalMonthValid
      dateOfDisposalYear.value = DateOfDisposalYearValid

      click on consent
      click on lossOfRegistrationConsent
      click on dispose

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display validation errors when day and year are input but no month" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage
      dateOfDisposalDay.value = DateOfDisposalDayValid
      dateOfDisposalYear.value = DateOfDisposalYearValid

      click on consent
      click on lossOfRegistrationConsent
      click on dispose

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display validation errors when day and month are input but no year" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage
      dateOfDisposalDay.value = DateOfDisposalDayValid
      dateOfDisposalMonth.value = DateOfDisposalMonthValid

      click on consent
      click on lossOfRegistrationConsent
      click on dispose

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display validation errors when day month and year are not input but all " +
      "other mandatory fields have been" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage

      click on consent
      click on lossOfRegistrationConsent
      click on dispose

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

    "does not proceed when milage has non-numeric (Html5Validation enabled)" taggedAs UiTag in new WebBrowser(
        app = fakeAppWithHtml5ValidationEnabledConfig,
        webDriver = WebDriverFactory.webDriver(targetBrowser = "firefox", javascriptEnabled = true)) {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage
      mileage enter MileageInvalid
      dateOfDisposalDay select DateOfDisposalDayValid
      dateOfDisposalMonth select DateOfDisposalMonthValid
      dateOfDisposalYear select DateOfDisposalYearValid
      click on consent
      click on lossOfRegistrationConsent

      click on dispose

      page.url should equal(DisposePage.url)
      ErrorPanel.hasErrors should equal(false)
    }*/

    "display one validation error message when milage has non-numeric (Html5Validation disabled)" taggedAs UiTag in
      new WebBrowserForSelenium(app = fakeAppWithHtml5ValidationDisabledConfig) {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage
      mileage.value = MileageInvalid
      dateOfDisposalDay.value = DateOfDisposalDayValid
      dateOfDisposalMonth.value = DateOfDisposalMonthValid
      dateOfDisposalYear.value = DateOfDisposalYearValid
      click on consent
      click on lossOfRegistrationConsent

      click on dispose

      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  "back button" should {
    "display previous page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage

      click on back

      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  "javascript disabled" should {
    "not display the Use Todays Date checkbox" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().
        vehicleLookupFormModel()

      webDriver.getPageSource shouldNot contain(TodaysDateOfDisposal)
    }
  }

  "use today's date" should {
    // This test needs to run with javaScript enabled.
    "fill in the date fields" taggedAs UiTag ignore new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage

      click on useTodaysDate

      dateOfDisposalDay.value should equal(DateOfDisposalDayValid)
      dateOfDisposalMonth.value should equal(DateOfDisposalMonthValid)
      dateOfDisposalYear.value should equal(DateOfDisposalYearValid)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      vehicleAndKeeperDetailsModel()


  private val fakeAppWithHtml5ValidationDisabledConfig =
    LightFakeApplication(TestGlobal, Map("html5Validation.enabled" -> false))

}
