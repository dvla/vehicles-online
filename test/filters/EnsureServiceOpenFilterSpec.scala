package filters

import com.google.inject.Guice
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.HeaderNames
import play.api.mvc.{Cookie, Cookies, RequestHeader, Results, SimpleResult}
import play.api.test.FakeRequest
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, InvalidSessionException}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.existentials
import uk.gov.dvla.vehicles.presentation.common.filters.EnsureSessionCreatedFilter
import helpers.UnitSpec
import utils.helpers.Config

class EnsureServiceOpenFilterSpec extends UnitSpec {

  val v = new Config
  println (">>>>>> Opening" + v.opening)

  "Create a session if there is not one" in setUp {

    case SetUp(filter, request, sessionFactory, nextFilter) =>

      val filterResult: Future[SimpleResult] = filter.apply(nextFilter)(request)
      whenReady(filterResult) { result =>

        println(result.header.status)

      }
      verify(sessionFactory, never()).getSession(any())
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

  private def setUp(test: SetUp => Any) {
    val sessionFactory = mock[ClientSideSessionFactory]
    val injector = Guice.createInjector(new ScalaModule {
      override def configure(): Unit = bind[ClientSideSessionFactory].toInstance(sessionFactory)
    })

    test(SetUp(
      filter = injector.getInstance(classOf[EnsureServiceOpenFilter]),
      request = FakeRequest(),
      sessionFactory = sessionFactory,
      nextFilter = new MockFilter()
    ))
  }
}