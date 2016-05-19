package views.disposal_of_vehicle

import composition.{TestGlobalWithFilters, TestHarness}
import helpers.UiSpec
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.scalatest.selenium.WebBrowser
import WebBrowser.go
import WebBrowser.pageSource
import pages.common.AlternateLanguages.{isCymraegDisplayed, isEnglishDisplayed}
import pages.disposal_of_vehicle.BeforeYouStartPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

final class MainUiSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the 'Cymraeg' language button and not the 'English' language button when the play language " +
      "cookie has value 'en'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage // By default will load in English.
      CookieFactoryForUISpecs.withLanguageEn()
      go to BeforeYouStartPage

      isCymraegDisplayed should equal(true)
      isEnglishDisplayed should equal(false)
    }

    "display the 'English' language button and not the 'Cymraeg' language button when the play language " +
      "cookie has value 'cy'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage // By default will load in English.
      CookieFactoryForUISpecs.withLanguageCy()
      go to BeforeYouStartPage

      isCymraegDisplayed should equal(false)
      isEnglishDisplayed should equal(true)
    }

    "display the 'Cymraeg' language button and not the 'English' language button when the play language cookie " +
      "does not exist (assumption that the browser default language is English)" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      isCymraegDisplayed should equal(true)
      isEnglishDisplayed should equal(false)
    }

    abstract class PrototypeFalse extends WebBrowserForSelenium(app = fakeAppWithPrototypeFalse)

    "not display prototype message when config set to false" taggedAs UiTag in new PrototypeFalse {
      go to BeforeYouStartPage

      pageSource should not include """<div class="prototype">"""
    }
  }

  private val fakeAppWithPrototypeFalse = LightFakeApplication(TestGlobalWithFilters,Map("prototype.disclaimer" -> "false"))
}