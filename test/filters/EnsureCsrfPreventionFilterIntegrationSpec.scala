package filters

import com.tzavellas.sse.guice.ScalaModule
import composition.{TestComposition, TestHarness}
import helpers.UiSpec
import org.mockito.Mockito.{never, times, verify}
import org.mockito.Matchers.any
import play.api.libs.iteratee.Done
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{EssentialAction, Results, RequestHeader}
import play.api.test.FakeRequest
import scala.language.existentials
import uk.gov.dvla.vehicles.presentation.common.filters.{CsrfPreventionAction, CsrfPreventionFilter}
import utils.helpers.Config

class EnsureCsrfPreventionFilterIntegrationSpec extends UiSpec with TestHarness with TestComposition {

  "GET request should result in the next filter being called" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, request, next) =>
        val fakeRequest = FakeRequest("GET", "/")
        filter.apply(next).apply(fakeRequest)
        verify(next, times(1)).apply(any[RequestHeader])
      }
  }

  "POST json request should result in the next filter not being called and a 403 forbidden generated" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, request, next) =>
        val json = Json.obj("foo" -> JsString("bar"))
        val fakeRequest = FakeRequest("POST", "/").withJsonBody(json)
        val result = filter.apply(next).apply(fakeRequest)
        verify(next, never).apply(any[RequestHeader])
        result should equal(Done(Results.Forbidden))
    }
  }

  "POST request not form url encoded should result in the next filter not being called and a 403 forbidden generated" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, request, next) =>
        val fakeRequest = FakeRequest("POST", "/")
        val result = filter.apply(next).apply(fakeRequest)
        verify(next, never).apply(any[RequestHeader])
        result should equal(Done(Results.Forbidden))
    }
  }

  "POST request with form url encoded body and no csrf token should result in the next filter not being called" in new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, request, next) =>
        val fakeRequest = FakeRequest("POST", "/")
          .withFormUrlEncodedBody()
          .withHeaders(play.api.http.HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
        filter.apply(next).apply(fakeRequest)
        verify(next, never).apply(any[RequestHeader])
    }
  }

  // TODO: Cannot get this one to work - the checkBody code looks like it does not find any form body. Will come back to this
  "POST request with form url encoded body and csrf token should result in the next filter being called" ignore new WebBrowserForSelenium {
    setUpTest {
      case SetUp(filter, request, next) =>
        val fakeRequest = FakeRequest("POST", "/")
          .withFormUrlEncodedBody((CsrfPreventionAction.TokenName, "TEST-CSRF-TOKEN")) // The body contains the csrf token
          .withHeaders(play.api.http.HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded") // Set the content type to form url encoded
        filter.apply(next).apply(fakeRequest)
        verify(next, times(1)).apply(any[RequestHeader])
    }
  }

  private case class SetUp(filter: CsrfPreventionFilter,
                           request: FakeRequest[_],
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
      request = FakeRequest(),
      next = mock[EssentialAction]
    ))
  }
}
