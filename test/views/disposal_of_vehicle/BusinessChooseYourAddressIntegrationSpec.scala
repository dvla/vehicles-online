package views.disposal_of_vehicle

import composition.TestHarness
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.UiSpec
import models.EnterAddressManuallyFormModel
import EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.currentUrl
import WebBrowser.go
import WebBrowser.pageSource
import WebBrowser.pageTitle
import pages.common.ErrorPanel
import pages.common.AlternateLanguages.{isCymraegDisplayed, isEnglishDisplayed}
import pages.common.Feedback.FeedbackLink
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage.{back, happyPath, manualAddress, sadPath}
import pages.disposal_of_vehicle.EnterAddressManuallyPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag
import webserviceclients.fakes.FakeAddressLookupService
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid

final class BusinessChooseYourAddressIntegrationSpec extends UiSpec with TestHarness {
  "business choose your address page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      pageTitle should equal(BusinessChooseYourAddressPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      pageSource.contains(FeedbackLink) should equal(true)
    }

    "redirect when no traderBusinessName is cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to BusinessChooseYourAddressPage

      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "display appropriate content when address service returns addresses" taggedAs UiTag in new WebBrowserForSelenium {
      SetupTradeDetailsPage.happyPath()
      pageSource.contains("No addresses found for that postcode") should equal(false) // Does not contain message
      pageSource should include("""<a id="enterAddressManuallyButton"""")
    }

    "display the postcode entered in the previous page" taggedAs UiTag in new WebBrowserForSelenium {
      SetupTradeDetailsPage.happyPath()
      pageSource.contains(FakeAddressLookupService.PostcodeValid.toUpperCase) should equal(true)
    }

    "display expected addresses in dropdown when address service returns addresses" taggedAs UiTag in new WebBrowserForSelenium {
      SetupTradeDetailsPage.happyPath()

      pageSource should include(
        s"presentationProperty stub, 123, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      pageSource should include(
        s"presentationProperty stub, 456, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      pageSource should include(
        s"presentationProperty stub, 789, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
    }

    "display appropriate content when address service returns no addresses" taggedAs UiTag in new WebBrowserForSelenium {
      SetupTradeDetailsPage.submitPostcodeWithoutAddresses

      pageSource should include("No addresses found for that postcode") // Does not contain the positive message
      pageSource should include("""<a id="enterAddressManuallyButton"""")
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      SetupTradeDetailsPage.happyPath()
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "manualAddress button that is displayed when addresses have been found" should {
    "go to the manual address entry page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      click on manualAddress

      pageTitle should equal(EnterAddressManuallyPage.title)
    }
  }

  "manualAddress button that is displayed when no addresses have been found" should {
    "go to the manual address entry page" taggedAs UiTag in new WebBrowserForSelenium {
      SetupTradeDetailsPage.submitPostcodeWithoutAddresses

      click on manualAddress

      pageTitle should equal(EnterAddressManuallyPage.title)
    }
  }

  "back button" should {
    "display previous page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      click on back

      pageTitle should equal(SetupTradeDetailsPage.title)
      currentUrl should equal(SetupTradeDetailsPage.url)
    }

    "display previous page with ceg route" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().withIdentifier("ceg")
      go to BusinessChooseYourAddressPage

      click on back

      pageTitle should equal(SetupTradeDetailsPage.title)
      currentUrl should equal(SetupTradeDetailsPage.cegUrl)
    }

  }

  "select button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath

      pageTitle should equal(VehicleLookupPage.title)
    }

    "display validation error messages when addressSelected is not in the list" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      sadPath

      ErrorPanel.numberOfErrors should equal(1)
    }

    "remove redundant EnterAddressManually cookie (as we are now in an alternate history)" taggedAs UiTag in new PhantomJsByDefault {
      def cacheSetupVisitedEnterAddressManuallyPage()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupTradeDetails().enterAddressManually()

      go to BeforeYouStartPage
      cacheSetupVisitedEnterAddressManuallyPage()
      happyPath

      // Verify the cookies identified by the full set of cache keys have been removed
      webDriver.manage().getCookieNamed(EnterAddressManuallyCacheKey) should equal(null)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.setupTradeDetails()
}
