package views.disposal_of_vehicle

import composition.TestHarness
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import models.AllCacheKeys
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.ErrorPage.startAgain
import pages.disposal_of_vehicle.{BeforeYouStartPage, ErrorPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

final class ErrorIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ErrorPage
      pageTitle should equal(ErrorPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ErrorPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }
  }

  "submit button" should {
    "remove redundant cookies when 'start again' button is clicked" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      go to ErrorPage
      click on startAgain
      // Verify the cookies identified by the full set of cache keys have been removed
      AllCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      businessChooseYourAddress().
      dealerDetails().
      vehicleAndKeeperDetailsModel().
      disposeFormModel().
      disposeTransactionId().
      vehicleRegistrationNumber()
}
