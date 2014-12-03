package controllers.disposal_of_vehicle

import com.tzavellas.sse.guice.ScalaModule
import controllers.{SurveyUrl, DisposeSuccess}
import controllers.disposal_of_vehicle.Common.PrototypeHtml
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import org.joda.time.Instant
import org.mockito.Mockito.when
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import models.DisposeFormModel.{PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import webserviceclients.fakes.FakeDateServiceImpl

import scala.concurrent.duration.DurationInt

final class DisposeSuccessUnitSpec extends UnitSpec {
  implicit val dateService = new DateServiceImpl
  val testDuration = 7.days.toMillis

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to SetUpTradeDetails on present when cache is empty" in new WithApplication {
      val request = FakeRequest()
      val result = disposeSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only DealerDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only VehicleDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only DisposeDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = disposeSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only VehicleDetails and DisposeDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel())
      val result = disposeSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only VehicleDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to SetUpTradeDetails on present when only DisposeDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "display the page with correctly formatted mileage on present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel(mileage = Some(123456))).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormTimestamp())
      val result = disposeSuccess.present(request)
      contentAsString(result) should include("123,456")
    }

    "display the page with mileage not entered message on present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormTimestamp())
      val result = disposeSuccess.present(request)
      contentAsString(result) should include("NOT ENTERED")
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      implicit val surveyUrl = new SurveyUrl()
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val disposeSuccessPrototypeNotVisible = new DisposeSuccess()

      val result = disposeSuccessPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "offer the survey on first successful dispose" in new WithApplication {
      implicit val config = mockSurveyConfig()

      val disposeSuccess = disposeWithMockConfig(config)

      contentAsString(disposeSuccess.present(requestFullyPopulated)) should include(config.prototypeSurveyUrl)
    }

    "not offer the survey for one just after the initial survey offering" in new WithApplication {
      implicit val config = mockSurveyConfig()

      val aMomentAgo = (Instant.now.getMillis - 100).toString

      val disposeSuccess = disposeWithMockConfig(config)
      contentAsString(disposeSuccess.present(
        requestFullyPopulated.withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(aMomentAgo))
      )) should not include config.prototypeSurveyUrl
    }

    "offer the survey one week after the first offering" in new WithApplication {
      implicit val config = mockSurveyConfig()

      val moreThen7daysAgo = (Instant.now.getMillis - config.prototypeSurveyPrepositionInterval - 1.minute.toMillis).toString

      val disposeSuccess = disposeWithMockConfig(config)
      contentAsString(disposeSuccess.present(
        requestFullyPopulated.withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(moreThen7daysAgo))
      )) should include(config.prototypeSurveyUrl)
    }

    "not offer the survey one week after the first offering" in new WithApplication {
      implicit val config = mockSurveyConfig()

      val lessThen7daysАgo = (Instant.now.getMillis - config.prototypeSurveyPrepositionInterval + 1.minute.toMillis).toString

      val disposeSuccess = disposeWithMockConfig(config)
      contentAsString(disposeSuccess.present(
        requestFullyPopulated.withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(lessThen7daysАgo))
      )) should not include config.prototypeSurveyUrl
    }

//    "not offer the survey if the survey url is not set in the config" in new WithApplication {
//      implicit val config: Config = mock[Config]
//      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
//      implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
//      implicit val dateService = injector.getInstance(classOf[DateService])
//      val disposeSuccessFake = new DisposeSuccess()
//      val presentFake = disposeSuccessFake.present(requestFullyPopulated)
//
//      when(config.prototypeSurveyUrl).thenReturn("")
//      when(config.prototypeSurveyPrepositionInterval).thenReturn(testDuration)
//      contentAsString(presentFake) should not include "survey"
//    }
  }

  "newDisposal" should {
    "redirect to correct next page after the new disposal button is clicked" in new WithApplication {
      val result = disposeSuccess.newDisposal(requestFullyPopulated)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when cache is empty" in new WithApplication {
      val request = FakeRequest()
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only DealerDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only VehicleDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only DisposeDetails are cached" in new WithApplication {
      val request = FakeRequest()
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only VehicleDetails and DisposeDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only VehicleDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetUpTradeDetails on submit when only DisposeDetails and DealerDetails are cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeSuccess.newDisposal(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "write interstitial cookie with BeforeYouStart url" in new WithApplication {
      val result = disposeSuccess.newDisposal(requestFullyPopulated)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.exists(c => c.name == PreventGoingToDisposePageCacheKey) should equal(true)
      }
    }
  }

  "exit" should {
    "redirect to BeforeYouStartPage" in new WithApplication {
      val result = disposeSuccess.exit(requestFullyPopulated)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "write interstitial cookie with BeforeYouStart url" in new WithApplication {
      val result = disposeSuccess.exit(requestFullyPopulated)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.exists(c => c.name == PreventGoingToDisposePageCacheKey) should equal(true)
      }
    }

    "set the surveyRequestTriggerDate to the current date" in new WithApplication {
      val result = disposeWithMockConfig(mockSurveyConfig("http://www.google.com")).exit(requestFullyPopulated)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        val surveyTime = cookies.find(_.name == SurveyRequestTriggerDateCacheKey).get.value.toLong
        surveyTime should be <= System.currentTimeMillis()
        surveyTime should be > System.currentTimeMillis() - 1000
      }
    }
  }

  private lazy val disposeSuccess = injector.getInstance(classOf[DisposeSuccess])
  private val requestFullyPopulated = FakeRequest().
    withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
    withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
    withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
    withCookies(CookieFactoryForUnitSpecs.disposeFormModel()).
    withCookies(CookieFactoryForUnitSpecs.disposeTransactionId()).
    withCookies(CookieFactoryForUnitSpecs.vehicleRegistrationNumber()).
    withCookies(CookieFactoryForUnitSpecs.disposeFormTimestamp())
  private lazy val present = disposeSuccess.present(requestFullyPopulated)

  def disposeWithMockConfig(config: Config): DisposeSuccess =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[DisposeSuccess])

  def mockSurveyConfig(url: String = "http://test/survery/url"): Config = {
    val config = mock[Config]
    val surveyUrl = url
    when(config.prototypeSurveyUrl).thenReturn(surveyUrl)
    when(config.prototypeSurveyPrepositionInterval).thenReturn(testDuration)
    config
  }
}
