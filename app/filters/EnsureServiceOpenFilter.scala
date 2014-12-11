package filters

import java.util.Locale

import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Filter, RequestHeader, Results}
import scala.concurrent.Future
import org.joda.time.{DateTimeZone, DateTime}
import play.api.mvc.Result
import scala.concurrent.ExecutionContext.Implicits.global
import utils.helpers.Config
import com.google.inject.Inject
import ServiceOpen.whitelist

class EnsureServiceOpenFilter @Inject()(implicit config: Config, dateTimeZone: DateTimeZoneService) extends Filter {

  private val millisPerHour = 3600000
  private lazy val opening = config.opening * millisPerHour
  private lazy val closing = config.closing * millisPerHour

  override def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if (whitelist.exists(requestHeader.path.contains)) nextFilter(requestHeader)
    else if (!serviceOpen())
      Future(Results.Ok(views.html.disposal_of_vehicle.closed(hour(opening), hour(closing))))
    else nextFilter(requestHeader)
  }

  def serviceOpen(currentDateTime: DateTime = new DateTime(dateTimeZone.currentDateTimeZone)): Boolean = {
    isNotSunday(currentDateTime) && isDuringOpeningHours(currentDateTime.getMillisOfDay)
  }

  def isNotSunday(day: DateTime): Boolean = day.getDayOfWeek != 7

  def isDuringOpeningHours(timeInMillis: Int): Boolean =
    if (closing >= opening) (timeInMillis >= opening) && (timeInMillis < closing)
    else (timeInMillis >= opening) || (timeInMillis < closing)

  private def hour(hour: Long) =
    DateTimeFormat.forPattern("HH:mm").withLocale(Locale.UK)
      .print(new DateTime(hour, DateTimeZone.forID("UTC"))).toLowerCase // Must use UTC as we only want to format the hour
}

trait DateTimeZoneService {
  def currentDateTimeZone: DateTimeZone
}

class DateTimeZoneServiceImpl extends DateTimeZoneService {
  override def currentDateTimeZone = DateTimeZone.forID("Europe/London")
}
