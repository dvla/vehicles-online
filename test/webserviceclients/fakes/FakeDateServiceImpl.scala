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
  final val DateOfDisposalDayValid = "25"
  final val DateOfDisposalMonthValid = "11"
  final val DateOfDisposalYearValid = "2014"
  final val TodayDay = DateTime.now.dayOfMonth.get.toString
  final val TodayMonth = DateTime.now.monthOfYear.get.toString
  final val TodayYear = DateTime.now.year.get.toString
}
