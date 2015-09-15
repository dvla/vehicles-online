package views.disposal_of_vehicle

import composition.TestHarness
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, go, pageTitle, pageSource}
import pages.common.AlternateLanguages.{isCymraegDisplayed, isEnglishDisplayed}
import pages.disposal_of_vehicle.BeforeYouStartPage.startNow
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage}
import ProgressBar.progressStep
import models.AllCacheKeys

final class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium{
      go to BeforeYouStartPage
      pageTitle should equal(BeforeYouStartPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      pageSource.contains(progressStep(1)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      pageSource.contains(progressStep(1)) should equal(false)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in
      new PhantomJsByDefault {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupTradeDetails().
          businessChooseYourAddress().
          enterAddressManually().
          dealerDetails().
          vehicleAndKeeperDetailsModel().
          disposeFormModel().
          disposeTransactionId().
          vehicleRegistrationNumber().
          preventGoingToDisposePage("").
          disposeOccurred

      go to BeforeYouStartPage
      cacheSetup()
      go to BeforeYouStartPage

      // Verify the cookies identified by the full set of cache keys have been removed
      AllCacheKeys.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }

  "display the 'Cymraeg' language button and not the 'English' language button when the play language cookie has " +
    "value 'en'" taggedAs UiTag in new WebBrowserForSelenium {
    go to BeforeYouStartPage // By default will load in English.
    CookieFactoryForUISpecs.withLanguageEn()
    go to BeforeYouStartPage

    isCymraegDisplayed should equal(true)
    isEnglishDisplayed should equal(false)
  }

  "display the 'English' language button and not the 'Cymraeg' language button when the play language cookie has "  +
    "value 'cy'" taggedAs UiTag in new WebBrowserForSelenium {
    go to BeforeYouStartPage // By default will load in English.
    CookieFactoryForUISpecs.withLanguageCy()
    go to BeforeYouStartPage

    isCymraegDisplayed should equal(false)
    isEnglishDisplayed should equal(true)
    pageTitle should equal(BeforeYouStartPage.titleCy)
  }

  "display the 'Cymraeg' language button and not the 'English' language button and mailto when the play language " +
    "cookie does not exist (assumption that the browser default language is English)" taggedAs UiTag in
    new WebBrowserForSelenium {
    go to BeforeYouStartPage

    isCymraegDisplayed should equal(true)
    isEnglishDisplayed should equal(false)
  }

  "startNow button" should {
    "go to next page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on startNow
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }
}
