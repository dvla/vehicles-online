package controllers.disposal_of_vehicle

import play.api.test.Helpers._
import pages.disposal_of_vehicle._
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.UnitSpec
import composition.TestComposition.{testInjector => injector}
import helpers.WithApplication

final class DisposeSuccessUnitSpec extends UnitSpec {
  "present" should {
    "display the page" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel("x" * 20))

      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.status should equal(OK)
      }
    }

    "redirect to SetUpTradeDetails on present when cache is empty" in new WithApplication {
      val request = FakeCSRFRequest()
      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only DealerDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only VehicleDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only DisposeDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel())
      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only VehicleDetails and DisposeDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel())
      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only VehicleDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only DisposeDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel())
      val result = disposeSuccess.present(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  "submit" should {
    "redirect to correct next page after the new disposal button is clicked" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel("x" * 20))
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to correct next page after the exit button is clicked" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel())
      val result = disposeSuccess.exit(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when cache is empty" in new WithApplication {
      val request = FakeCSRFRequest()
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only DealerDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only VehicleDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only DisposeDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.disposeModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only VehicleDetails and DisposeDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only VehicleDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only DisposeDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeCSRFRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  private val disposeSuccess = injector.getInstance(classOf[DisposeSuccess])
}
