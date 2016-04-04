package filters

import com.tzavellas.sse.guice.ScalaModule
import composition.{TestComposition, TestHarness}
import helpers.UiSpec
import org.apache.commons.codec.binary.Base64
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{EssentialAction, RequestHeader, Result, Results}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.await
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import play.api.test.Helpers.writeableOf_AnyContentAsFormUrlEncoded
import play.api.test.Helpers.writeableOf_AnyContentAsJson
import scala.language.existentials
import uk.gov.dvla.vehicles.presentation.common.filters.{CsrfPreventionAction, CsrfPreventionFilter}
import utils.helpers.Config

class EnsureCsrfPreventionFilterIntegrationSpec extends UiSpec with TestHarness with TestComposition {

  "GET request should result in the next filter being called" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, next) =>
        val fakeRequest = FakeRequest("GET", "/before-you-start")
        val result = await(Helpers.call(filter.apply(next), fakeRequest))
        result should equal(Results.Ok)
    }
  }

  "POST json request should result in the next filter not being called and a 403 forbidden generated" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, next) =>
        val json = Json.obj("foo" -> JsString("bar"))
        val fakeRequest = FakeRequest("POST", "/setup-trade-details").withJsonBody(json)
        val result = await(Helpers.call(filter.apply(next), fakeRequest))
        result should equal(Results.Forbidden)
    }
  }

  "POST request not form url encoded should result in the next filter not being called and a 403 forbidden generated" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, next) =>
        val fakeRequest = FakeRequest("POST", "/vehicle-lookup")
        val result = await(Helpers.call(filter.apply(next), fakeRequest))
        result should equal(Results.Forbidden)
    }
  }


  "POST request with form url encoded body and no csrf token should result in the next filter not being called" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, next) =>
        val fakeRequest = FakeRequest("POST", "/complete-and-confirm")
          .withFormUrlEncodedBody()
          .withHeaders(play.api.http.HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
        val result = await(Helpers.call(filter.apply(next), fakeRequest))
        result should equal(Results.Forbidden)
    }
  }

  "POST request with form url encoded body and csrf token should result in the next filter being called" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, next) =>
        val fakeRequest = FakeRequest("POST", "/confirm")
          .withFormUrlEncodedBody((CsrfPreventionAction.TokenName, validCsrfToken)) // The body contains the csrf token
          .withHeaders(play.api.http.HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")

        val result = await(Helpers.call(filter.apply(next), fakeRequest))
        result should equal(Results.Ok)
    }
  }

  "POST request with token in url and csrf token should result in the next filter being called" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, next) =>
        val base64Encoded = Base64.encodeBase64String(validCsrfToken.getBytes)
        val uriEncoded = play.utils.UriEncoding.encodePathSegment(base64Encoded, "UTF-8")
        val fakeRequest = FakeRequest("POST", s"/confirm/${uriEncoded}")
          .withFormUrlEncodedBody(("TEST", "TESTCONTENT"))
          .withCookies(play.api.mvc.Cookie(play.api.http.HeaderNames.REFERER, "INVALID"))

        val result = await(Helpers.call(filter.apply(next), fakeRequest))
        result should equal(Results.Ok)
    }
  }

  private val validCsrfToken = "2ec506394e2810ff32d9eeadd3f8c78339f3b4e7" +
    "-1459753683001" +
    "-zwYpnaSiqxJbWY9Hxuw6L/DldKAqB/IUAHFp9F5cIKbpesOkgI5HxcinpaA9S8p09d1wewKNTFoA59SBqHsO4Q=="

  private case class SetUp(filter: CsrfPreventionFilter,
                           next: EssentialAction)

  private def setUpTest(test: SetUp => Any): Unit = {

    val injector = testInjector(new ScalaModule {
      override def configure(): Unit = {
        val mockConfig = mock[Config]
        bind[Config].toInstance(mockConfig)
      }
    })

    // Call the function and pass it a new instance of the SetUp case class, which is in its signature
    test(SetUp(
      filter = injector.getInstance(classOf[CsrfPreventionFilter]),
      next = EssentialAction(nextAction)
    ))
  }

  private def nextAction: (RequestHeader) => Iteratee[Array[Byte], Result] = { requestHeader =>
    Iteratee.fold[Array[Byte], Result](Results.NotFound) {
      (length, bytes) => Results.Ok
    }
  }
}
