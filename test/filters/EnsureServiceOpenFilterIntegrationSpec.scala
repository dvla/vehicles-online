package filters

import com.tzavellas.sse.guice.ScalaModule
import composition.{TestComposition, TestHarness}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import scala.concurrent.Future
import scala.language.existentials
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiSpec
import utils.helpers.Config

class EnsureServiceOpenFilterIntegrationSpec extends UiSpec with TestHarness with ScalaFutures with TestComposition {
  // The filter chain will return null if we redirect to the closed page.
  "Return a null next filter request if trying to access the service out of hours" in new WebBrowserForSelenium{
    setUpOutOfHours {
      case SetUp(filter, request, nextFilter) =>
        val filterResult: Future[Result] = filter.apply(nextFilter)(request)
        whenReady(filterResult) { result =>
          nextFilter.passedRequest should be(null)
        }
    }
  }

  "Return a valid next filter request if trying to access the service within acceptable hours" in new WebBrowserForSelenium{
    setUpInHours {
      case SetUp(filter, request, nextFilter) =>
        val filterResult: Future[Result] = filter.apply(nextFilter)(request)
        whenReady(filterResult) { result =>
          nextFilter.passedRequest.toString() should equal("GET /")
        }
    }
  }

  private class MockFilter extends ((RequestHeader) => Future[Result]) {
    var passedRequest: RequestHeader = _
    override def apply(rh: RequestHeader): Future[Result] = {
      passedRequest = rh
      Future.successful(Results.Ok)
    }
  }

  private case class SetUp(filter: ServiceOpenFilter,
                           request: FakeRequest[_],
                           nextFilter: MockFilter)

  private def setUpInHours(test: SetUp => Any): Unit = {
    setUpOpeningHours(test, 0, 1439)
  }

  private def setUpOutOfHours(test: SetUp => Any): Unit = {
    setUpOpeningHours(test, 1, 1)
  }

  private def setUpOpeningHours(test: SetUp => Any, opening: Int = 0, closing: Int = 1439): Unit = {

    val injector = testInjector(new ScalaModule {
      override def configure(): Unit = {
        val mockConfig = org.scalatest.mock.MockitoSugar.mock[Config]
        when(mockConfig.openingTimeMinOfDay).thenReturn(opening)
        when(mockConfig.closingTimeMinOfDay).thenReturn(closing)
        when(mockConfig.closedDays).thenReturn(List[Int]())
        when(mockConfig.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
        when(mockConfig.assetsUrl).thenReturn(None) // Stub this config value.
        bind[Config].toInstance(mockConfig)
      }
    })

    // Call the function and pass it a new instance of the SetUp case class, which is in its signature
    test(SetUp(
      filter = injector.getInstance(classOf[ServiceOpenFilter]),
      request = FakeRequest(),
      nextFilter = new MockFilter()
    ))
  }
}
