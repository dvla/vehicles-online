package views.disposal_of_vehicle

import composition.{TestGlobalWithFilters, TestHarness}
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.UiSpec
import models.DisposeFormModelBase.Form.TodaysDateOfDisposal
import models.DisposeFormModelBase.Form.EmailOptionId
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.currentUrl
import WebBrowser.go
import WebBrowser.pageTitle
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
import pages.disposal_of_vehicle.DisposePage.emailInvisible
import pages.disposal_of_vehicle.DisposePage.emailVisible
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDateServiceImpl.TodayDay
import webserviceclients.fakes.FakeDateServiceImpl.TodayMonth
import webserviceclients.fakes.FakeDateServiceImpl.TodayYear
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageInvalid

final class DisposeIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposePage

      pageTitle should equal(title)
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

    "display optional email for trade" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposePage
      emailInvisible.isDisplayed should be (true)
      emailVisible.isDisplayed should be (true)
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
      "javascript enabled" taggedAs UiTag in new WebBrowserWithJs {
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
      "the date of disposal with javascript enabled" taggedAs UiTag in  new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup().vehicleLookupFormModel()
      go to DisposePage

      click on useTodaysDate

      dateOfDisposalDay.value should equal(TodayDay)
      dateOfDisposalMonth.value should equal(TodayMonth)
      dateOfDisposalYear.value should equal(TodayYear)

      click on consent
      click on lossOfRegistrationConsent

      dispose.underlying.getAttribute("class") should not include "disabled"

      click on DisposePage.emailInvisible

      click on dispose

      // We want to wait for the javascript to execute and redirect to the next page. For build servers we may need to
      // wait longer than the default.
      val timeout: Span = scaled(Span(2, Seconds))
      implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = timeout)

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
      click on DisposePage.emailInvisible

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
      click on DisposePage.emailInvisible
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
      click on DisposePage.emailInvisible
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
      click on DisposePage.emailInvisible
      click on dispose

      ErrorPanel.numberOfErrors should equal(1)
    }

    "does not proceed when milage has non-numeric (Html5Validation enabled)" taggedAs UiTag in new WebBrowserForSelenium(
      app = fakeAppWithHtml5ValidationEnabledConfig) {
    go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage
      mileage.value = MileageInvalid
      dateOfDisposalDay.value = DateOfDisposalDayValid
      dateOfDisposalMonth.value = DateOfDisposalMonthValid
      dateOfDisposalYear.value = DateOfDisposalYearValid
      click on consent
      click on lossOfRegistrationConsent
      click on DisposePage.emailInvisible

      click on dispose

      currentUrl should equal(DisposePage.url)
      ErrorPanel.numberOfErrors should equal(1)
    }

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
      click on DisposePage.emailInvisible

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
    "fill in the date fields" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposePage

      click on useTodaysDate

      dateOfDisposalDay.value should equal(TodayDay)
      dateOfDisposalMonth.value should equal(TodayMonth)
      dateOfDisposalYear.value should equal(TodayYear)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      vehicleAndKeeperDetailsModel()


  private val fakeAppWithHtml5ValidationDisabledConfig =
    LightFakeApplication(TestGlobalWithFilters, Map("html5Validation.enabled" -> false))

  private val fakeAppWithHtml5ValidationEnabledConfig =
    LightFakeApplication(TestGlobalWithFilters, Map("html5Validation.enabled" -> true))

}
