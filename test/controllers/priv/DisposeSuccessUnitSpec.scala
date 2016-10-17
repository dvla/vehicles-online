package controllers.priv

import com.tzavellas.sse.guice.ScalaModule
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, TestWithApplication}
import models.DisposeFormModel.{PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import org.joda.time.Instant
import org.mockito.Mockito.when
import pages.disposal_of_vehicle.priv.NotifyAnotherSalePage
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import scala.concurrent.duration.DurationInt
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import utils.helpers.Config
import webserviceclients.fakes.FakeDateServiceImpl

class DisposeSuccessUnitSpec extends UnitSpec {
  "present" should {
    "display the page" in new TestWithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }
  }

  "newDisposal" should {
    "redirect to correct next page after the new disposal button is clicked" in new TestWithApplication {
      val result = disposeSuccess.newDisposal(requestFullyPopulated)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(NotifyAnotherSalePage.address))
      }
    }
  }

  private implicit lazy val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
  private implicit lazy val config = injector.getInstance(classOf[Config])
  private implicit lazy val dateService = injector.getInstance(classOf[DateService])

  private lazy val disposeSuccess = new DisposeSuccess()(
    clientSideSessionFactory = clientSideSessionFactory,
    config = config,
    surveyUrl = new controllers.SurveyUrl(),
    dateService = dateService
  )

  private lazy val requestFullyPopulated = FakeRequest()
    .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
    .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
    .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    .withCookies(CookieFactoryForUnitSpecs.privateDisposeFormModel())
    .withCookies(CookieFactoryForUnitSpecs.disposeTransactionId())
    .withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber())
    .withCookies(CookieFactoryForUnitSpecs.disposeFormTimestamp())
  private lazy val present = disposeSuccess.present(requestFullyPopulated)
}
