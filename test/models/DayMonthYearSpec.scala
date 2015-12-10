package models

import helpers.UnitSpec
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.language.postfixOps
import scala.util.Try
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear

class DayMonthYearSpec extends UnitSpec {
  val validFourDigitYear = 1984
 
  "DayMonthYear" should {
    "return the correct 'yyyy-MM-dd' date format" in {
      val dmy = DayMonthYear(1, 1, validFourDigitYear)
      dmy.`yyyy-MM-dd` shouldEqual validFourDigitYear.toString + "-01-01"
    }

    "Format to dd/MM/yyyy of 26-6-validFourDigitYear should give 26/06/validFourDigitYear" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      dmy.`dd/MM/yyyy` shouldEqual "26/06/" + validFourDigitYear.toString
    }

    "Format to yyyy-MM-dd of 26-6-validFourDigitYear should give validFourDigitYear-06-26" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      dmy.`yyyy-MM-dd` shouldEqual validFourDigitYear + "-06-26"
    }

    "Format to yyyy-MM-dd of empty DayMonthYear should give empty" in {
      val dmy = DayMonthYear(0, 0, 0)
      dmy.`yyyy-MM-dd` should equal("")
    }

    "Format to yyyy-MM-dd'T'HH:mm:00" in {
      val dmy = DayMonthYear(30, 5, validFourDigitYear, Some(9), Some(45))
      dmy.`yyyy-MM-dd'T'HH:mm:00` should equal(validFourDigitYear + "-05-30T09:45:00")
    }

    "format as '23 September, 2013' " in {
      val dmy = DayMonthYear(23, 9, validFourDigitYear)
      dmy.`dd month, yyyy` shouldEqual "23 September, " + validFourDigitYear
    }

    """accept format "01 September, 2001" """ in {
      Try(DateTimeFormat.forPattern("dd MMMM, yyyy").parseDateTime("01 September, 2001")).isSuccess should equal(true)
    }

    """accept format "01 September 2001" """ in {
      Try(DateTimeFormat.forPattern("dd MMMM yyyy").parseDateTime("01 September 2001")).isSuccess should equal(true)
    }

    """reject format "31 February 2001" """ in {
      Try(DateTimeFormat.forPattern("dd MMMM yyyy").parseDateTime("31 February 2001")).isFailure should equal(true)
    }

    "convert to a valid date time" in {
      val year = validFourDigitYear
      val month = 11
      val day = 25
      val dayMonthYear = DayMonthYear(day = day, month = month, year = year)
      dayMonthYear.toDateTime.isEmpty should equal(false) // Indicates we get a Some[T] back from the Option[T]
      dayMonthYear.toDateTime.get should equal(new DateTime(year, month, day, 0, 0))
    }

    "not convert to a valid date time when DayMonthYear contains invalid day" in {
      val dayMonthYear = DayMonthYear(day = 32, month = 11, year = validFourDigitYear)
      dayMonthYear.toDateTime.isEmpty should equal(true) // Indicates we get a None back from the Option[T]
    }

    "not convert to a valid date time when DayMonthYear contains invalid month" in {
      val dayMonthYear = DayMonthYear(day = 25, month = 13, year = validFourDigitYear)
      dayMonthYear.toDateTime.isEmpty should equal(true)
    }

    "not convert to a valid date time when DayMonthYear contains invalid year" in {
      val tooBigYear : Int = org.joda.time.Years.MAX_VALUE.getYears + 1
      val dayMonthYear = DayMonthYear(day = 25, month = 11, year = tooBigYear)
      dayMonthYear.toDateTime.isEmpty should equal(true)
    }
  }

  "compareTo" should {
    "return less than a date 1 year in the future" in {
      val present = DayMonthYear(1, 1, validFourDigitYear)
      val futureDate = DayMonthYear(1, 1, validFourDigitYear + 1)
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 month in the future" in {
      val present = DayMonthYear(1, 1, validFourDigitYear)
      val futureDate = DayMonthYear(1, 2, validFourDigitYear)
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 day in the future" in {
      val present = DayMonthYear(1, 1, validFourDigitYear)
      val futureDate = DayMonthYear(2, 1, validFourDigitYear)
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 hour in the future" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, Some(0))
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 minute in the future" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, Some(0), Some(0))
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, Some(0), Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date when hour not specified in present" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, None)
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date when minute not specified in present" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, Some(0), None)
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, Some(0), Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return false when dates are equal" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, Some(0), Some(0))
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, Some(0), Some(0))
      present.compare(futureDate) shouldEqual 0
      present < futureDate shouldEqual false
      present > futureDate shouldEqual false
    }

    "return false when dates are equal but no hours" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, None)
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, None)
      present.compare(futureDate) shouldEqual 0
      present < futureDate shouldEqual false
      present > futureDate shouldEqual false
    }

    "return false when dates are equal but no minutes" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, Some(0), None)
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, Some(0), None)
      present.compare(futureDate) shouldEqual 0
      present < futureDate shouldEqual false
      present > futureDate shouldEqual false
    }

    "return greater than a date when hour not specified in future" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, Some(1))
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, None)
      present < futureDate shouldEqual false
      present > futureDate shouldEqual true
    }

    "return greater than a date when minute not specified in future" in {
      val present = DayMonthYear(1, 1, validFourDigitYear, Some(0), Some(1))
      val futureDate = DayMonthYear(1, 1, validFourDigitYear, Some(0), None)
      present < futureDate shouldEqual false
      present > futureDate shouldEqual true
    }
  }

  "toDateTime" should {
    "return None given an invalid date" in {
      DayMonthYear(32, 13, validFourDigitYear).toDateTime match {
        case Some(_) => fail("should not have parsed")
        case None =>
      }
    }

    "return Some given a valid date" in {
      DayMonthYear(1, 1, validFourDigitYear).toDateTime match {
        case Some(_) =>
        case None => fail("should have parsed")
      }
    }
  }

  "minus" should {
    "return unchanged if DateTime is invalid" in {
      val dmy = DayMonthYear(32, 13, validFourDigitYear)
      (dmy - 1 day) shouldEqual dmy
    }

    "subtract day" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 1 day) shouldEqual DayMonthYear(25, 6, validFourDigitYear)
    }

    "subtract week" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 1 week) shouldEqual DayMonthYear(19, 6, validFourDigitYear)
    }

    "subtract week with change in month" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 1 week) shouldEqual DayMonthYear(19, 6, validFourDigitYear)
    }

    "subtract week with change in month and year" in {
      val dmy = DayMonthYear(1, 1, validFourDigitYear)
      (dmy - 1 week) shouldEqual DayMonthYear(25, 12, validFourDigitYear - 1)
    }

    "subtract month" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 1 month) shouldEqual DayMonthYear(26, 5, validFourDigitYear)
    }

    "subtract month with chnage in day" in {
      val dmy = DayMonthYear(31, 10, validFourDigitYear)
      (dmy - 1 month) shouldEqual DayMonthYear(30, 9, validFourDigitYear)
    }

    "subtract days" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 6 days) shouldEqual DayMonthYear(20, 6, validFourDigitYear)
      (dmy - 6 day) shouldEqual DayMonthYear(20, 6, validFourDigitYear)
    }

    "subtract weeks" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 2 weeks) shouldEqual DayMonthYear(12, 6, validFourDigitYear)
    }

    "subtract weeks with change in month" in {
      val dmy = DayMonthYear(8, 12, validFourDigitYear)
      (dmy - 2 weeks) shouldEqual DayMonthYear(24, 11, validFourDigitYear)
    }

    "subtract months without year decrement" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 5 months) shouldEqual DayMonthYear(26, 1, validFourDigitYear)
    }

    "subtract months giving year decrement" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 6 months) shouldEqual DayMonthYear(26, 12, validFourDigitYear - 1)
    }

    "subtract years" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      (dmy - 1 years) shouldEqual DayMonthYear(26, 6, validFourDigitYear - 1)
    }

    //complex subtration - confirmed via date calculator http://www.timeanddate.com/date/dateadd.html
    "subtract days, months, weeks and year with a change in each" in {
      val dmy = DayMonthYear(26, 6, validFourDigitYear)
      ((((dmy - 60 days) - 5 weeks) - 13 months) - 2 year) shouldEqual DayMonthYear(23, 2, validFourDigitYear - 3)
    }
  }

  "from" should {
    "accept a Joda DateTime" in {
      val dmy = DayMonthYear.from(new DateTime(validFourDigitYear, 2, 23, 0, 0))

      dmy.day should equal(23)
      dmy.month should equal(2)
      dmy.year should equal(validFourDigitYear)
    }
  }

  "withTime" should {
    "include time" in {
      val dmyWithTime = DayMonthYear(23, 9, validFourDigitYear).withTime(hour = 14, minutes = 55)
      dmyWithTime shouldEqual DayMonthYear(23, 9, validFourDigitYear, Some(14), Some(55))
    }
  }
}