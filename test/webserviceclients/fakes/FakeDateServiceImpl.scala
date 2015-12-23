package webserviceclients.fakes

import org.joda.time.{DateTime, Instant}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear

final class FakeDateServiceImpl extends DateService {
  import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
  import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
  import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid

  override def today = DayMonthYear(
    DateOfDisposalDayValid.toInt,
    DateOfDisposalMonthValid.toInt,
    DateOfDisposalYearValid.toInt
  )

  override def now = Instant.now()

  override def dateTimeISOChronology: String = new DateTime(
    DateOfDisposalYearValid.toInt,
    DateOfDisposalMonthValid.toInt,
    DateOfDisposalDayValid.toInt,
    0,
    0).toString
}

object FakeDateServiceImpl {
  private final val today = DateTime.now
  private final val dod = today.minusYears(1)

  final val DateOfDisposalDayValid = dod.toString("dd")
  final val DateOfDisposalMonthValid = dod.toString("MM")
  final val DateOfDisposalYearValid = dod.toString("YYYY")

  final val TodayDay = today.toString("dd")
  final val TodayMonth = today.toString("MM")
  final val TodayYear = today.toString("YYYY")
}
