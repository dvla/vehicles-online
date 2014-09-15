package views.disposal_of_vehicle

import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.disposal_of_vehicle.VrmLockedPage.{exit, newDisposal}
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage, VrmLockedPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import models.{DisposeCacheKeys, AllCacheKeys}

final class VrmLockedUiSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      page.title should equal(VrmLockedPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      page.source should not contain ProgressBar.div
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VrmLockedPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "newDisposal button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on newDisposal

      page.title should equal(VehicleLookupPage.title)
    }

    "redirect to setuptradedetails when no trade details are cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      click on newDisposal

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "remove redundant cookies" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on newDisposal

      // Verify the cookies identified by the dispose set of cache keys have been removed
      DisposeCacheKeys.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  "exit button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on exit

      page.title should equal(BeforeYouStartPage.title)
    }

    "remove redundant cookies" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

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
      bruteForcePreventionViewModel()
}
