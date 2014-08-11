package filters

import com.google.inject.Guice
import com.tzavellas.sse.guice.ScalaModule
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.mockito.Mockito.{never, verify, when, mock}
import org.scalatest.concurrent.{ScalaFutures, Futures}
import play.api.mvc.{Results, SimpleResult, RequestHeader}
import play.api.test.FakeRequest
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.existentials



import scala.concurrent.Future

final class EnsureServiceOpenFilterIntegrationSpec extends UiSpec with TestHarness with ScalaFutures{
  // Returning a 503 is a proxy measure for redirection to the closed page
  "Return a service unavailable status if trying to access the service out of hours" in new WebBrowser {
    setUpOutOfHours {
      case SetUp(filter, request, sessionFactory, nextFilter) =>
        val filterResult: Future[SimpleResult] = filter.apply(nextFilter)(request)
        whenReady(filterResult) { result =>
          result.header.status should be(503)
        }
    }
  }

  // Returning a 200 is a proxy measure for continuing to the requested page
    "Return an OK if trying to access the service within acceptable hours" in new WebBrowser{
      setUpInHours {
        case SetUp(filter, request, sessionFactory, nextFilter) =>
          val filterResult: Future[SimpleResult] = filter.apply(nextFilter)(request)
          whenReady(filterResult) { result =>
            result.header.status should be(200)
          }
      }
    }

  private class MockFilter extends ((RequestHeader) => Future[SimpleResult]) {
    var passedRequest: RequestHeader = _

    override def apply(rh: RequestHeader): Future[SimpleResult] = {
      passedRequest = rh
      Future(Results.Ok)
    }
  }

  private case class SetUp(filter: EnsureServiceOpenFilter,
                           request: FakeRequest[_],
                           sessionFactory:ClientSideSessionFactory,
                           nextFilter: MockFilter)

  private def setUpInHours(test: SetUp => Any) {
    val sessionFactory =  org.scalatest.mock.MockitoSugar.mock[ClientSideSessionFactory]

    val injector = Guice.createInjector(new ScalaModule {
      override def configure(): Unit = {
        bind[ClientSideSessionFactory].toInstance(sessionFactory)
        val mockConfig = org.scalatest.mock.MockitoSugar.mock[Config]
        when(mockConfig.opening).thenReturn(0)
        when(mockConfig.closing).thenReturn(24)
        bind[Config].toInstance(mockConfig)
      }
    })

    test(SetUp(
      filter = injector.getInstance(classOf[EnsureServiceOpenFilter]),
      request = FakeRequest(),
      sessionFactory = sessionFactory,
      nextFilter = new MockFilter()
    ))
  }

  private def setUpOutOfHours(test: SetUp => Any) {
    val sessionFactory =  org.scalatest.mock.MockitoSugar.mock[ClientSideSessionFactory]

    val injector = Guice.createInjector(new ScalaModule {
      override def configure(): Unit = {
        bind[ClientSideSessionFactory].toInstance(sessionFactory)
        val mockConfig = org.scalatest.mock.MockitoSugar.mock[Config]
        when(mockConfig.opening).thenReturn(1)
        when(mockConfig.closing).thenReturn(1)
        bind[Config].toInstance(mockConfig)
      }
    })

    test(SetUp(
      filter = injector.getInstance(classOf[EnsureServiceOpenFilter]),
      request = FakeRequest(),
      sessionFactory = sessionFactory,
      nextFilter = new MockFilter()
    ))
  }


}
