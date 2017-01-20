package views.disposal_of_vehicle

import composition.TestHarness
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebElement}
import org.scalatest.selenium.WebBrowser
import WebBrowser.go
import WebBrowser.pageTitle
import pages.common.{Accessibility, ErrorPanel}
import pages.common.AlternateLanguages.{isCymraegDisplayed, isEnglishDisplayed}
import pages.disposal_of_vehicle.SetupTradeDetailsPage.happyPath
import pages.disposal_of_vehicle.{BeforeYouStartPage, BusinessChooseYourAddressPage, SetupTradeDetailsPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.model.SetupTradeDetailsFormModel
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

final class SetUpTradeDetailsIntegrationSpec extends UiSpec with TestHarness {
  "got to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to SetupTradeDetailsPage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to SetupTradeDetailsPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "display the 'Cymraeg' language button and not the 'English' language button when the language " +
      "cookie is set to 'en'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage // By default will load in English.
      CookieFactoryForUISpecs.withLanguageEn()
      go to SetupTradeDetailsPage

      isCymraegDisplayed should equal(true)
      isEnglishDisplayed should equal(false)
    }

    "display the 'English' language button and not the 'Cymraeg' language button when the language " +
      "cookie is set to 'cy'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage // By default will load in English.
      CookieFactoryForUISpecs.withLanguageCy()
      go to SetupTradeDetailsPage

      isCymraegDisplayed should equal(false)
      isEnglishDisplayed should equal(true)
    }
  }

  "lookup button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath()
      pageTitle should equal(BusinessChooseYourAddressPage.title)
    }

    "display two summary validation error messages when no details are entered" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = "", traderBusinessPostcode = "")
      ErrorPanel.numberOfErrors should equal(2)
    }

    "add aria required attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsFormModel.Form.TraderNameId) should equal(true)
    }

    "add aria invalid attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsFormModel.Form.TraderNameId) should equal(true)
    }

    "add aria required attribute to trader postcode field when required field not input" taggedAs UiTag in
      new WebBrowserForSelenium {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsFormModel.Form.TraderPostcodeId) should equal(true)
    }

    "add aria invalid attribute to trader postcode field when required field not input" taggedAs UiTag in
      new WebBrowserForSelenium {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsFormModel.Form.TraderPostcodeId) should equal(true)
    }
  }
}
