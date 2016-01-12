package controllers

import Common.PrototypeHtml
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import webserviceclients.fakes.FakeDateServiceImpl

class VrmLockedUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(play.api.http.Status.OK)
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val vrmLockedPrototypeNotVisible = new VrmLocked()

      val result = vrmLockedPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "newDisposal" should {
    "redirect to vehicle lookup page after the new disposal " +
      "button is clicked when the expected data is in the cookies" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vrmLocked.tryAnother(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to setup trade details page after the new disposal button " +
      "is clicked when the expected data is not in the cookies" in new WithApplication {
      val request = FakeRequest()
      val result = vrmLocked.tryAnother(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  "exit" should {
    "redirect to correct next page after the exit button is clicked" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vrmLocked.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private lazy val vrmLocked = injector.getInstance(classOf[VrmLocked])

  private lazy val present = {
    val dateService = new FakeDateServiceImpl
    val request = FakeRequest()
      .withCookies(CookieFactoryForUnitSpecs.bruteForcePreventionViewModel(
        dateTimeISOChronology = dateService.dateTimeISOChronology)
      )
    vrmLocked.present(request)
  }
}
