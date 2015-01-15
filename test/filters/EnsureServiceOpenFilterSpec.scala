package filters

import java.util.Locale

import com.google.inject.Guice
import com.tzavellas.sse.guice.ScalaModule
import helpers.{WithApplication, UnitSpec}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.filters.{DateTimeZoneServiceImpl, DateTimeZoneService}
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.existentials

class EnsureServiceOpenFilterSpec extends UnitSpec {

  "Return True for an acceptable time, with the current timezone as GMT" in {
    val dateTime = new DateTime()
    setUpInHours((setup: SetUp) => {
      setup.filter.serviceOpen(dateTime) should equal(true)
    }, dateTime)
  }

  "Return False for an out of hours time, with the current timezone as GMT" in {
    val dateTime = new DateTime()
    setUpOutOfHours((setup: SetUp) => {
      setup.filter.serviceOpen(dateTime) should equal(false)
    }, dateTime)
  }

  "Return True for a timezone time falling within opening hours, and False for a time in another timezone falling outside opening hours" in {
    setUpInHours ((setup: SetUp) => {
        setup.filter.serviceOpen() should equal(true)
    }, new DateTimeZoneServiceImpl)

    val nonDefaultTimeZoneService = new DateTimeZoneService {
      override def currentDateTimeZone: DateTimeZone = DateTimeZone.forOffsetHours(outOfHoursOffset(inHoursOffset))
    }

    setUpInHours ((setup: SetUp) => {
        setup.filter.serviceOpen() should equal(false)
    }, nonDefaultTimeZoneService)
  }

  "The out of hours message contains the hours from the config" in new WithApplication {
    val requestHeader = mock[RequestHeader]
    when(requestHeader.path).thenReturn("some-test-path")
    val next = (request:RequestHeader) => Future.successful[Result](throw new Exception("Should not come here"))
    val dateTime = new DateTime()
    setUpOutOfHours((setup: SetUp) => {
      val result = setup.filter.apply(next)(requestHeader)
      val resultString = contentAsString(result)
      resultString should include(Messages("disposal.closed.p2", h(setup.opening), h(setup.closing)))
    }, dateTime)

    setUpOutOfHours((setup: SetUp) => {
      val result = setup.filter.apply(next)(requestHeader)
      val resultString = contentAsString(result)
      resultString should include(Messages("disposal.closed.p2", h(setup.opening), h(setup.closing)))
    }, dateTime, new DateTimeZoneService {
      override def currentDateTimeZone = DateTimeZone.forID("Europe/London")
    })

    def h(hour: Long) =
      DateTimeFormat.forPattern("HH:mm").withLocale(Locale.UK)
        .print(new DateTime(hour * 3600000, DateTimeZone.forID("UTC"))).toLowerCase
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

  private case class SetUp(filter: ServiceOpenFilter,
                           request: FakeRequest[_],
                           sessionFactory:ClientSideSessionFactory,
                           nextFilter: MockFilter,
                           opening: Int,
                           closing: Int)

  private def setUpInHours(test: SetUp => Any, dateTime: DateTime): Unit = {
    val opening = dateTime.getHourOfDay
    val closing = dateTime.getHourOfDay + 1
    setUp(test, opening, closing)
  }

  private def setUpInHours(test: SetUp => Any, dateTimeZoneService: DateTimeZoneService): Unit = {
    setUp(test, 8, 18, dateTimeZoneService)
  }

  private def setUpOutOfHours(test: SetUp => Any, dateTime: DateTime): Unit = {
    val opening = dateTime.getHourOfDay - 2
    val closing = dateTime.getHourOfDay - 1
    setUp(test, opening, closing)
  }

  private def setUpOutOfHours(test: SetUp => Any,
                              dateTime: DateTime,
                              dateTimeZoneService: DateTimeZoneService): Unit = {
    val opening = dateTime.getHourOfDay - 2
    val closing = dateTime.getHourOfDay - 1
    setUp(test, opening, closing, dateTimeZoneService)
  }

  private def setUp(test: SetUp => Any,
                    opening: Int = 0,
                    closing: Int = 24,
                    dateTimeZoneService: DateTimeZoneService = new DateTimeZoneServiceImpl) {
    val sessionFactory =  org.scalatest.mock.MockitoSugar.mock[ClientSideSessionFactory]

    val injector = Guice.createInjector(new ScalaModule {
      override def configure(): Unit = {
        bind[ClientSideSessionFactory].toInstance(sessionFactory)
        val mockConfig = org.scalatest.mock.MockitoSugar.mock[Config]
        when(mockConfig.opening).thenReturn(opening)
        when(mockConfig.closing).thenReturn(closing)
        when(mockConfig.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
        bind[Config].toInstance(mockConfig)
        bind[DateTimeZoneService].toInstance(dateTimeZoneService)
      }
    })

    test(SetUp(
      filter = injector.getInstance(classOf[ServiceOpenFilter]),
      request = FakeRequest(),
      sessionFactory = sessionFactory,
      nextFilter = new MockFilter(),
      opening,
      closing
    ))
  }

}
