package controllers

import com.tzavellas.sse.guice.ScalaModule
import helpers.{TestWithApplication, UnitSpec}
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import org.joda.time.Instant
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import utils.helpers.Config

class SurveyUrlSpec extends UnitSpec {
  private val surveyUrlString = "http://surveyUrl"
  private val privateKeeperSurveyUrlString = "http://privateKeeperSurveyUrl"

  "when trader" should {
    "get the business survey url if configured so and for the first time" in new TestWithApplication {
      val (surveyUrl, request) =
        setUp(surveyUrlString, privateKeeperSurveyUrlString, 10000, None)

      surveyUrl(request, isPrivateKeeper = false) should equal(Some(surveyUrlString))
    }

    "Not get the survey url if within the timeout " in new TestWithApplication {
      val (surveyUrl, request) =
        setUp(surveyUrlString, privateKeeperSurveyUrlString, 10000, Some(Instant.now.getMillis - 100))

      surveyUrl(request, isPrivateKeeper = false) should equal(None)
    }

    "Get the survey url after timeout expire" in new TestWithApplication {
      val (surveyUrl, request) =
        setUp(surveyUrlString, privateKeeperSurveyUrlString, 100, Some(Instant.now.getMillis - 1000))

      surveyUrl(request, isPrivateKeeper = false) should equal(Some(surveyUrlString))
    }
  }

  "when private keeper" should {
    "get the business survey url if configured so and for the first time" in new TestWithApplication {
      val (surveyUrl, request) =
        setUp(surveyUrlString, privateKeeperSurveyUrlString, 10000, None)

      surveyUrl(request, isPrivateKeeper = true) should equal(Some(privateKeeperSurveyUrlString))
    }

    "Not get the survey url if within the timeout " in new TestWithApplication {
      val (surveyUrl, request) =
        setUp(surveyUrlString, privateKeeperSurveyUrlString, 10000, Some(Instant.now.getMillis - 100))

      surveyUrl(request, isPrivateKeeper = true) should equal(None)
    }

    "Get the survey url after timeout expire" in new TestWithApplication {
      val (surveyUrl, request) =
        setUp(surveyUrlString, privateKeeperSurveyUrlString, 100, Some(Instant.now.getMillis - 1000))

      surveyUrl(request, isPrivateKeeper = true) should equal(Some(privateKeeperSurveyUrlString))
    }
  }

  private def setUp(surveyUrl: String,
                    privateKeeperSurveyUrl: String,
                    interval: Long,
                    lastShown: Option[Long]) = {
    implicit val config: Config = mock[Config]
    val request = lastShown.fold(FakeRequest()){ lastShown =>
      FakeRequest().withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(lastShown.toString))
    }

    val injector = testInjector(new ScalaModule(){
      override def configure(): Unit = {
        bind[Config].toInstance(config)
      }
    })
    when(config.surveyUrl).thenReturn(surveyUrl)
    when(config.privateKeeperSurveyUrl).thenReturn(privateKeeperSurveyUrl)
    when(config.prototypeSurveyPrepositionInterval).thenReturn(interval)
    (injector.getInstance(classOf[SurveyUrl]), request)
  }
}
