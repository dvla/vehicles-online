package views.disposal_of_vehicle

import csrfprevention.filters
import csrfprevention.filters.CsrfPreventionAction
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import pages.disposal_of_vehicle.{BeforeYouStartPage, ErrorPage}
import org.openqa.selenium.{By, WebElement, WebDriver}
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import pages.disposal_of_vehicle.ErrorPage.startAgain
import viewmodels.AllCacheKeys

final class ErrorIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to ErrorPage

      page.title should equal(ErrorPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue  {
      go to BeforeYouStartPage
      cacheSetup()

      go to ErrorPage

      page.source should not contain ProgressBar.div
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to ErrorPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "submit button" should {
    "remove redundant cookies when 'start again' button is clicked" taggedAs UiTag in new WebBrowser {
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
      vehicleDetailsModel().
      disposeFormModel().
      disposeTransactionId().
      vehicleRegistrationNumber()
}
