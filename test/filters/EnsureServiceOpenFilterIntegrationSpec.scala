package filters

import com.google.inject.Guice
import com.google.inject.util.Modules
import com.tzavellas.sse.guice.ScalaModule
import composition.TestModule
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.existentials

final class EnsureServiceOpenFilterIntegrationSpec extends UiSpec with TestHarness with ScalaFutures{
  // The filter chain will return null if we redirect to the closed page.
  "Return a null next filter request if trying to access the service out of hours" in new WebBrowser {
    setUpOutOfHours {
      case SetUp(filter, request, sessionFactory, nextFilter) =>
        val filterResult: Future[Result] = filter.apply(nextFilter)(request)
        whenReady(filterResult) { result =>
          nextFilter.passedRequest should be(null)
        }
    }
  }

  "Return a valid next filter request if trying to access the service within acceptable hours" in new WebBrowser{
    setUpInHours {
      case SetUp(filter, request, sessionFactory, nextFilter) =>
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
      Future(Results.Ok)
    }
  }

  private case class SetUp(filter: ServiceOpenFilter,
                           request: FakeRequest[_],
                           sessionFactory:ClientSideSessionFactory,
                           nextFilter: MockFilter)

  private def setUpInHours(test: SetUp => Any) {
    setUpOpeningHours(test, 0, 24)
  }

  private def setUpOutOfHours(test: SetUp => Any) = {
    setUpOpeningHours(test, 1, 1)
  }

  private def setUpOpeningHours(test: SetUp => Any, opening: Int = 0, closing: Int = 24) {
    val sessionFactory = org.scalatest.mock.MockitoSugar.mock[ClientSideSessionFactory]

    val injector = Guice.createInjector(
      Modules.`override`(new TestModule()).`with`(
        new ScalaModule {
          override def configure(): Unit = {
            bind[ClientSideSessionFactory].toInstance(sessionFactory)
            val mockConfig = org.scalatest.mock.MockitoSugar.mock[Config]
            when(mockConfig.opening).thenReturn(opening)
            when(mockConfig.closing).thenReturn(closing)
            bind[Config].toInstance(mockConfig)
          }
        }
      )
    )

    test(SetUp(
      filter = injector.getInstance(classOf[ServiceOpenFilter]),
      request = FakeRequest(),
      sessionFactory = sessionFactory,
      nextFilter = new MockFilter()
    ))
  }

}
