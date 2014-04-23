package views.disposal_of_vehicle

import pages.disposal_of_vehicle._
import helpers.webbrowser.TestHarness
import helpers.disposal_of_vehicle.CacheSetup
import pages.common.ErrorPanel
import helpers.UiSpec
import services.fakes.FakeAddressLookupService._
import VehicleLookupPage.{happyPath, back}

class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

  "VehicleLookupIntegrationSpec Integration" should {

    "be presented" in new WebBrowser {
      cacheSetup()

      go to VehicleLookupPage

      assert(page.title equals VehicleLookupPage.title)
    }

    "Redirect when no traderBusinessName is cached" in new WebBrowser {
      go to VehicleLookupPage

      assert(page.title equals SetupTradeDetailsPage.title)
    }

    "go to the next page when correct data is entered" in new WebBrowser {
      cacheSetup()

      happyPath()

      assert(page.title equals DisposePage.title)
    }

    "display one validation error message when no referenceNumber is entered" in new WebBrowser {
      cacheSetup()

      happyPath(referenceNumber = "")

      assert(ErrorPanel.numberOfErrors equals 1)
    }

    "display one validation error message when no registrationNumber is entered" in new WebBrowser {
      cacheSetup()

      happyPath(registrationNumber = "")

      assert(ErrorPanel.numberOfErrors equals 1)
    }

    "display one validation error message when a registrationNumber is entered containing one character" in new WebBrowser {
      cacheSetup()

      happyPath(registrationNumber = "a")

      assert(ErrorPanel.numberOfErrors equals 1)
    }

    "display one validation error message when a registrationNumber is entered containing special characters" in new WebBrowser {
      cacheSetup()

      happyPath(registrationNumber = "$^")

      assert(ErrorPanel.numberOfErrors equals 1)
    }

    "display two validation error messages when no vehicle details are entered but consent is given" in new WebBrowser {
      cacheSetup()

      happyPath(referenceNumber = "", registrationNumber = "")

      assert(ErrorPanel.numberOfErrors equals 2)
    }

    "display one validation error message when only a valid referenceNumber is entered and consent is given" in new WebBrowser {
      cacheSetup()

      happyPath(registrationNumber = "")

      assert(ErrorPanel.numberOfErrors equals 1)
    }

    "display one validation error message when only a valid registrationNumber is entered and consent is given" in new WebBrowser {
      cacheSetup()

      happyPath(referenceNumber = "")

      assert(ErrorPanel.numberOfErrors equals 1)
    }

    "redirect when no dealerBusinessName is cached" in new WebBrowser {
      go to VehicleLookupPage

      assert(page.title equals SetupTradeDetailsPage.title)
    }

    "display previous page when back link is clicked with uprn present" in new WebBrowser {
      CacheSetup.setupTradeDetails()
      CacheSetup.businessChooseYourAddress(addressWithUprn)
      go to VehicleLookupPage

      click on back

      assert(page.title equals BusinessChooseYourAddressPage.title)
    }

    "display previous page when back link is clicked with no uprn present" in new WebBrowser {
      cacheSetup()
      go to VehicleLookupPage

      click on back

      assert(page.title equals EnterAddressManuallyPage.title)
    }
  }

  private def cacheSetup() = {
    CacheSetup.
      setupTradeDetails().
      businessChooseYourAddress()
  }
}