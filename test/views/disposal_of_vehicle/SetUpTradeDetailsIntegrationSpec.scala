package views.disposal_of_vehicle

import helpers.UiSpec
import helpers.common.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement}
import pages.common.{Accessibility, ErrorPanel}
import pages.disposal_of_vehicle.SetupTradeDetailsPage.happyPath
import pages.disposal_of_vehicle.{BusinessChooseYourAddressPage, SetupTradeDetailsPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import viewmodels.SetupTradeDetailsViewModel

final class SetUpTradeDetailsIntegrationSpec extends UiSpec with TestHarness {
  "got to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to SetupTradeDetailsPage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to SetupTradeDetailsPage

      page.source.contains(progressStep(2)) should equal(true)
    }

    "display the progress of the page when progress bar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to SetupTradeDetailsPage

      page.source.contains(progressStep(2)) should equal(false)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to SetupTradeDetailsPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "lookup button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
      happyPath()
      page.title should equal(BusinessChooseYourAddressPage.title)
    }

    "display two summary validation error messages when no details are entered" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = "", traderBusinessPostcode = "")
      ErrorPanel.numberOfErrors should equal(2)
    }

    "add aria required attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsViewModel.Form.TraderNameId) should equal(true)
    }

    "add aria invalid attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsViewModel.Form.TraderNameId) should equal(true)
    }

    "add aria required attribute to trader postcode field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsViewModel.Form.TraderPostcodeId) should equal(true)
    }

    "add aria invalid attribute to trader postcode field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsViewModel.Form.TraderPostcodeId) should equal(true)
    }
  }
}
