package filters

import play.api.mvc._
import scala.concurrent.Future
import org.joda.time.{DateTimeZone, DateTime}
import java.util.TimeZone
import play.api.mvc.SimpleResult
import scala.concurrent.ExecutionContext.Implicits.global
import utils.helpers.Config
import com.google.inject.Inject
import ServiceOpen.whitelist

class EnsureServiceOpenFilter @Inject()(implicit config: Config) extends Filter {

  private val millisPerHour = 3600000
  private lazy val opening = config.opening * millisPerHour
  private lazy val closing = config.closing * millisPerHour

  override def apply(nextFilter: (RequestHeader) => Future[SimpleResult])(requestHeader: RequestHeader): Future[SimpleResult] = {

    if (whitelist.exists(requestHeader.path.contains)) nextFilter(requestHeader)
    else if (!serviceOpen) Future(Results.Ok(views.html.disposal_of_vehicle.closed()))
         else nextFilter(requestHeader)
  }

  def serviceOpen: Boolean = {
    val agencyTime = new DateTime(DateTimeZone.UTC).plusMillis(dstOffsetMillis)
    val agencyTimeInMillis = agencyTime.getMillisOfDay
    isNotSunday(agencyTime) && isDuringOpeningHours(agencyTimeInMillis)
  }

  def isNotSunday(day: DateTime): Boolean = day.getDayOfWeek != 7

  def isDuringOpeningHours(timeInMillis: Int): Boolean = {
    if (closing >= opening) (timeInMillis >= opening) && (timeInMillis < closing)
    else (timeInMillis >= opening) || (timeInMillis < closing)
  }

  val dstOffsetMillis = TimeZone.getTimeZone("Europe/London").getDSTSavings
}