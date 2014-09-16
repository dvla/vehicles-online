package services

import org.joda.time.{DateTime, Instant}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear

final class DateServiceImpl extends DateService {
  override def today = DayMonthYear.today
  override def now = Instant.now()
  override def dateTimeISOChronology: String = DateTime.now().toString
}