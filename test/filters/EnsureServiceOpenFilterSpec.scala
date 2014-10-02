package filters

import com.google.inject.Guice
import com.tzavellas.sse.guice.ScalaModule
import helpers.UnitSpec
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.Matchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.HeaderNames
import play.api.mvc.{Cookie, Cookies, RequestHeader, Result, Results}
import play.api.test.FakeRequest
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, InvalidSessionException}
import utils.helpers.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.existentials

class EnsureServiceOpenFilterSpec extends UnitSpec {

  "Return True for an acceptable time, with the current timezone as GMT" in {
    val dateTime = new DateTime()
    setUpInHours ({
      case SetUp(filter, request, sessionFactory, nextFilter) =>
        filter.serviceOpen(dateTime) should equal(true)
    }, dateTime)
  }

  "Return False for an out of hours time, with the current timezone as GMT" in {
    val dateTime = new DateTime()
    setUpOutOfHours({
      case SetUp(filter, request, sessionFactory, nextFilter) =>
        filter.serviceOpen(dateTime) should equal(false)
    }, dateTime)
  }

  "Return True for a timezone time falling within opening hours, and False for a time in another timezone falling outside opening hours" in {
    val defaultTimeZone = DateTimeZone.getDefault
    DateTimeZone.setDefault(DateTimeZone.forOffsetHours(inHoursOffset))
    try {
      setUpInHours ({
        case SetUp(filter, request, sessionFactory, nextFilter) =>
          filter.serviceOpen() should equal(true)
      })

      DateTimeZone.setDefault(DateTimeZone.forOffsetHours(outOfHoursOffset(inHoursOffset)))
      setUpInHours ({
        case SetUp(filter, request, sessionFactory, nextFilter) =>
          filter.serviceOpen() should equal(false)
      })
    } finally {
      DateTimeZone.setDefault(defaultTimeZone)
    }
  }

  private class MockFilter extends ((RequestHeader) => Future[Result]) {
    var passedRequest: RequestHeader = _

    override def apply(rh: RequestHeader): Future[Result] = {
      passedRequest = rh
      Future(Results.Ok)
    }
  }

  private def inHoursOffset: Int = {
    val formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
    val targetDateTime = formatter.parseDateTime("01/01/2014 11:30:30")
    val currentDateTime = new DateTime()

    - (currentDateTime.hourOfDay.get() - targetDateTime.getHourOfDay)
  }

  private def outOfHoursOffset (inHoursOffset: Int): Int = {
    if (inHoursOffset > 0) {
      inHoursOffset - 12
    } else {
      inHoursOffset + 12
    }
  }

  private case class SetUp(filter: EnsureServiceOpenFilter,
                           request: FakeRequest[_],
                           sessionFactory:ClientSideSessionFactory,
                           nextFilter: MockFilter)

  private def setUpInHours(test: SetUp => Any, dateTime: DateTime): Unit = {
    val opening = dateTime.getHourOfDay
    val closing = dateTime.getHourOfDay + 1
    setUp(test, opening, closing)
  }

  private def setUpInHours(test: SetUp => Any): Unit = {
    setUp(test, 8, 18)
  }

  private def setUpOutOfHours(test: SetUp => Any, dateTime: DateTime): Unit = {
    val opening = dateTime.getHourOfDay - 2
    val closing = dateTime.getHourOfDay - 1
    setUp(test, opening, closing)
  }

  private def setUpOutOfHours(test: SetUp => Any): Unit = {
    setUp(test, 1, 1)
  }

  private def setUp(test: SetUp => Any, opening: Int = 0, closing: Int = 24) {
    val sessionFactory =  org.scalatest.mock.MockitoSugar.mock[ClientSideSessionFactory]

    val injector = Guice.createInjector(new ScalaModule {
      override def configure(): Unit = {
        bind[ClientSideSessionFactory].toInstance(sessionFactory)
        val mockConfig = org.scalatest.mock.MockitoSugar.mock[Config]
        when(mockConfig.opening).thenReturn(opening)
        when(mockConfig.closing).thenReturn(closing)
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