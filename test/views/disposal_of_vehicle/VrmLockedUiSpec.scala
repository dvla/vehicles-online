package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.disposal_of_vehicle.VrmLockedPage.{exit, newDisposal}
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage, VrmLockedPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import models.{DisposeCacheKeys, AllCacheKeys}

final class VrmLockedUiSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      pageTitle should equal(VrmLockedPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VrmLockedPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "contain time of locking" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText should include regex "^(\\d|0\\d|1\\d|2[0-3]):[0-5]\\d".r
    }

    "contain time of locking when JavaScript is disabled" taggedAs UiTag in new WebBrowserWithJsDisabled {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText should include regex "^(\\d|0\\d|1\\d|2[0-3]):[0-5]\\d".r
    }
  }

  "newDisposal button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on newDisposal

      pageTitle should equal(VehicleLookupPage.title)
    }

    "redirect to setuptradedetails when no trade details are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.bruteForcePreventionViewModel()
      go to VrmLockedPage

      click on newDisposal

      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "remove redundant cookies" taggedAs UiTag in new PhantomJsByDefault {
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
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage

      click on exit

      pageTitle should equal(BeforeYouStartPage.title)
    }

    "remove redundant cookies" taggedAs UiTag in new PhantomJsByDefault {
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
