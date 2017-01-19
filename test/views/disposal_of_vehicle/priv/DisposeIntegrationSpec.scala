package views.disposal_of_vehicle.priv

import composition.TestHarness
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.UiSpec
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.currentUrl
import WebBrowser.pageTitle
import pages.common.ErrorPanel
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.priv.{DisposeForPrivateKeeperPage, DisposeSuccessForPrivateKeeperPage}
import pages.disposal_of_vehicle.DisposePage.consent
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.dispose
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.dateOfDisposalDay
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.dateOfDisposalMonth
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.dateOfDisposalYear
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.emailInvisible
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.emailVisible
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.lossOfRegistrationConsent
import pages.disposal_of_vehicle.priv.DisposeForPrivateKeeperPage.title
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid

class DisposeIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeForPrivateKeeperPage

      // Additional URL check because the titles are the same for trader and private.
      currentUrl should equal(DisposeForPrivateKeeperPage.url)
      pageTitle should equal(title)
    }

    // Javascript needs to be enabled to do more sophisticated checking of visibility.
    "not display I have consent of the current keeper checkbox" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeForPrivateKeeperPage
      consent.isDisplayed should be (false)
    }

    "not display use today's date" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeForPrivateKeeperPage
      webDriver.findElements(By.id("todays_date")).isEmpty should be (true)
    }

    "display optional email for private keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to DisposeForPrivateKeeperPage
      emailInvisible.isDisplayed should be (true)
      emailVisible.isDisplayed should be (true)
    }
  }

  "dispose button" should {
    "display DisposeSuccess on correct submission with no email confirmation" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().vehicleLookupFormModel()

      go to DisposeForPrivateKeeperPage

      click on lossOfRegistrationConsent

      dateOfDisposalDay.value = DateOfDisposalDayValid
      dateOfDisposalMonth.value = DateOfDisposalMonthValid
      dateOfDisposalYear.value = DateOfDisposalYearValid

      click on emailInvisible

      click on dispose

      currentUrl should equal(DisposeSuccessForPrivateKeeperPage.url)
      pageTitle should equal(DisposeSuccessForPrivateKeeperPage.title)
    }

    "display validation errors when all mandatory fields entered but" +
      " no email preference selected" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to DisposeForPrivateKeeperPage

      click on lossOfRegistrationConsent

      dateOfDisposalDay.value = DateOfDisposalDayValid
      dateOfDisposalMonth.value = DateOfDisposalMonthValid
      dateOfDisposalYear.value = DateOfDisposalYearValid

      click on dispose

      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      vehicleAndKeeperDetailsModel()
}
