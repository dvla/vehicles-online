package models

import helpers.UnitSpec
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.language.postfixOps
import scala.util.Try
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear

class DayMonthYearSpec extends UnitSpec {
  "DayMonthYear" should {
    "return the correct 'yyyy-MM-dd' date format" in {
      val dmy = DayMonthYear(1, 1, 1963)
      dmy.`yyyy-MM-dd` shouldEqual "1963-01-01"
    }

    "Format to dd/MM/yyyy of 26-6-2010 should give 26/06/2010" in {
      val dmy = DayMonthYear(26, 6, 2010)
      dmy.`dd/MM/yyyy` shouldEqual "26/06/2010"
    }

    "Format to yyyy-MM-dd of 26-6-2010 should give 2010-06-26" in {
      val dmy = DayMonthYear(26, 6, 2010)
      dmy.`yyyy-MM-dd` shouldEqual "2010-06-26"
    }

    "Format to yyyy-MM-dd of empty DayMonthYear should give empty" in {
      val dmy = DayMonthYear(0, 0, 0)
      dmy.`yyyy-MM-dd` should equal("")
    }

    "Format to yyyy-MM-dd'T'HH:mm:00" in {
      val dmy = DayMonthYear(30, 5, 2002, Some(9), Some(45))
      dmy.`yyyy-MM-dd'T'HH:mm:00` should equal("2002-05-30T09:45:00")
    }

    "format as '23 September, 2013' " in {
      val dmy = DayMonthYear(23, 9, 2013)
      dmy.`dd month, yyyy` shouldEqual "23 September, 2013"
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
      val year = 2000
      val month = 11
      val day = 25
      val dayMonthYear = DayMonthYear(day = day, month = month, year = year)
      dayMonthYear.toDateTime.isEmpty should equal(false) // Indicates we get a Some[T] back from the Option[T]
      dayMonthYear.toDateTime.get should equal(new DateTime(year, month, day, 0, 0))
    }

    "not convert to a valid date time when DayMonthYear contains invalid day" in {
      val dayMonthYear = DayMonthYear(day = 99, month = 11, year = 2000)
      dayMonthYear.toDateTime.isEmpty should equal(true) // Indicates we get a None back from the Option[T]
    }

    "not convert to a valid date time when DayMonthYear contains invalid month" in {
      val dayMonthYear = DayMonthYear(day = 25, month = 99, year = 2000)
      dayMonthYear.toDateTime.isEmpty should equal(true)
    }

    "not convert to a valid date time when DayMonthYear contains invalid year" in {
      val dayMonthYear = DayMonthYear(day = 25, month = 99, year = 20001)
      dayMonthYear.toDateTime.isEmpty should equal(true)
    }
  }

  "compareTo" should {
    "return less than a date 1 year in the future" in {
      val present = DayMonthYear(1, 1, 2010)
      val futureDate = DayMonthYear(1, 1, 2011)
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 month in the future" in {
      val present = DayMonthYear(1, 1, 2010)
      val futureDate = DayMonthYear(1, 2, 2010)
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 day in the future" in {
      val present = DayMonthYear(1, 1, 2010)
      val futureDate = DayMonthYear(2, 1, 2010)
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 hour in the future" in {
      val present = DayMonthYear(1, 1, 2010, Some(0))
      val futureDate = DayMonthYear(1, 1, 2010, Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date 1 minute in the future" in {
      val present = DayMonthYear(1, 1, 2010, Some(0), Some(0))
      val futureDate = DayMonthYear(1, 1, 2010, Some(0), Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date when hour not specified in present" in {
      val present = DayMonthYear(1, 1, 2010, None)
      val futureDate = DayMonthYear(1, 1, 2010, Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return less than a date when minute not specified in present" in {
      val present = DayMonthYear(1, 1, 2010, Some(0), None)
      val futureDate = DayMonthYear(1, 1, 2010, Some(0), Some(1))
      present < futureDate shouldEqual true
      present > futureDate shouldEqual false
    }

    "return equal when dates are equal" in {
      val present = DayMonthYear(1, 1, 2010, Some(0), Some(0))
      val futureDate = DayMonthYear(1, 1, 2010, Some(0), Some(0))
      present.compare(futureDate) shouldEqual 0
      present < futureDate shouldEqual false
      present > futureDate shouldEqual false
    }

    "return equal when dates are equal but no hours" in {
      val present = DayMonthYear(1, 1, 2010, None)
      val futureDate = DayMonthYear(1, 1, 2010, None)
      present.compare(futureDate) shouldEqual 0
      present < futureDate shouldEqual false
      present > futureDate shouldEqual false
    }

    "return equal when dates are equal but no minutes" in {
      val present = DayMonthYear(1, 1, 2010, Some(0), None)
      val futureDate = DayMonthYear(1, 1, 2010, Some(0), None)
      present.compare(futureDate) shouldEqual 0
      present < futureDate shouldEqual false
      present > futureDate shouldEqual false
    }

    "return greater than a date when hour not specified in future" in {
      val present = DayMonthYear(1, 1, 2010, Some(1))
      val futureDate = DayMonthYear(1, 1, 2010, None)
      present < futureDate shouldEqual false
      present > futureDate shouldEqual true
    }

    "return greater than a date when minute not specified in future" in {
      val present = DayMonthYear(1, 1, 2010, Some(0), Some(1))
      val futureDate = DayMonthYear(1, 1, 2010, Some(0), None)
      present < futureDate shouldEqual false
      present > futureDate shouldEqual true
    }
  }

  "toDateTime" should {
    "return None given an invalid date" in {
      DayMonthYear(32, 13, 2010).toDateTime match {
        case Some(_) => fail("should not have parsed")
        case None =>
      }
    }

    "return Some given a valid date" in {
      DayMonthYear(1, 1, 2010).toDateTime match {
        case Some(_) =>
        case None => fail("should have parsed")
      }
    }
  }

  "minus" should {
    "return unchanged if DateTime is invalid" in {
      val dmy = DayMonthYear(32, 13, 2010)
      (dmy - 1 day) shouldEqual dmy
    }

    "subtract 1 day from 26-6-2010 to give 25-6-2010" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 1 day) shouldEqual DayMonthYear(25, 6, 2010)
    }

    "subtract 1 week from 26-6-2010 to give 19-6-2010" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 1 week) shouldEqual DayMonthYear(19, 6, 2010)
    }

    "subtract 1 month from 26-6-2010 to give 26-5-2010" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 1 month) shouldEqual DayMonthYear(26, 5, 2010)
    }

    "subtract 6 days from 26-6-2010 to give 20-6-2010" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 6 days) shouldEqual DayMonthYear(20, 6, 2010)
      (dmy - 6 day) shouldEqual DayMonthYear(20, 6, 2010)
    }

    "subtract 2 weeks from 26-6-2010 to give 12-6-2010" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 2 weeks) shouldEqual DayMonthYear(12, 6, 2010)
    }

    "subtract 5 months from 26-6-2010 to give 26-1-2010" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 5 months) shouldEqual DayMonthYear(26, 1, 2010)
    }

    "subtract 6 months from 26-6-2010 to give 26-12-2009" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 6 months) shouldEqual DayMonthYear(26, 12, 2009)
    }

    "subtract 14 years from 26-6-2010 to give 26-6-1996" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (dmy - 14 years) shouldEqual DayMonthYear(26, 6, 1996)
    }

    "subtract 2 days, 4 months and 1 year from 26-6-2010 to give 24-2-2009" in {
      val dmy = DayMonthYear(26, 6, 2010)
      (((dmy - 2 days) - 4 months) - 1 year) shouldEqual DayMonthYear(24, 2, 2009)
    }
  }

  "from" should {
    "accept a Joda DateTime" in {
      val dmy = DayMonthYear.from(new DateTime(2013, 2, 23, 0, 0))

      dmy.day should equal(23)
      dmy.month should equal(2)
      dmy.year should equal(2013)
    }
  }

  "withTime" should {
    "include time" in {
      val dmyWithTime = DayMonthYear(23, 9, 2013).withTime(hour = 14, minutes = 55)
      dmyWithTime shouldEqual DayMonthYear(23, 9, 2013, Some(14), Some(55))
    }
  }
}