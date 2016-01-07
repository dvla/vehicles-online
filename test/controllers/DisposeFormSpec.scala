package controllers

import composition.WithApplication
import helpers.UnitSpec
import models.DisposeFormModelBase.Form.ConsentId
import models.DisposeFormModelBase.Form.DateOfDisposalId
import models.DisposeFormModelBase.Form.LossOfRegistrationConsentId
import models.DisposeFormModelBase.Form.MileageId
import models.PrivateDisposeFormModel.Form.EmailOptionId
import org.joda.time.{Instant, LocalDate}
import org.mockito.invocation.InvocationOnMock
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.when
import org.mockito.stubbing.Answer
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import common.mappings.DayMonthYear.{DayId, MonthId, YearId}
import common.mappings.{OptionalToggle, Mileage}
import common.services.DateService
import common.views.models.DayMonthYear
import common.webserviceclients.emailservice.EmailService
import common.webserviceclients.emailservice.EmailServiceSendRequest
import common.webserviceclients.emailservice.EmailServiceSendResponse
import common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.dispose.{DisposeConfig, DisposeRequestDto, DisposeServiceImpl, DisposeWebService}
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.{ConsentValid, MileageValid, disposeResponseSuccess}
import webserviceclients.fakes.FakeResponse

class DisposeFormSpec extends UnitSpec {
  "form" should {
    "accept when all fields contain valid responses" in new WithApplication {
      val model = formWithValidDefaults().get

      model.mileage.get should equal(MileageValid.toInt)
      model.dateOfDisposal should equal(
        new LocalDate(DateOfDisposalYearValid.toInt,
          DateOfDisposalMonthValid.toInt,
          DateOfDisposalDayValid.toInt)
      )
      model.consent should equal(ConsentValid)
      model.lossOfRegistrationConsent should equal(ConsentValid)
    }

    "accept when all mandatory fields contain valid responses" in new WithApplication {
      val model = formWithValidDefaults(
        mileage = "",
        dayOfDispose = DateOfDisposalDayValid,
        monthOfDispose = DateOfDisposalMonthValid,
        yearOfDispose = DateOfDisposalYearValid).get

      model.mileage should equal(None)
      model.dateOfDisposal should equal(
        new LocalDate(DateOfDisposalYearValid.toInt,
          DateOfDisposalMonthValid.toInt,
          DateOfDisposalDayValid.toInt)
      )
    }
  }

  "mileage" should {
    "reject if mileage is more than maximum" in new WithApplication {
      formWithValidDefaults(mileage = (Mileage.Max + 1).toString).errors should have length 1
    }
    "reject if mileage is not numeric" in new WithApplication {
      formWithValidDefaults(mileage = "Boom").errors should have length 1
    }
  }

  "dateOfDisposal" should {
    "reject if date day is not selected" in new WithApplication {
      formWithValidDefaults(dayOfDispose = "").errors should have length 1
    }

    "reject if date month is not selected" in new WithApplication {
      formWithValidDefaults(monthOfDispose = "").errors should have length 1
    }

    "reject if date year is not selected" in new WithApplication {
      formWithValidDefaults(yearOfDispose = "").errors should have length 1
    }

    "reject if date is in the future" in new WithApplication {
      // Attempting to dispose with a date 1 day into the future.
      val tomorrow = LocalDate.now.plusDays(1)
      val result = formWithValidDefaults(
        dayOfDispose = tomorrow.toString("dd"),
        monthOfDispose = tomorrow.toString("MM"),
        yearOfDispose = tomorrow.getYear.toString)

      result.errors should have length 1
      result.errors.head.key should equal(DateOfDisposalId)
      result.errors.head.message should equal("error.date.inTheFuture")
    }

    "reject if date is more than 2 years in the past" in new WithApplication {
      val invalidDod = LocalDate.now.minusYears(2).minusDays(1)

      // Attempting to dispose with a date 2 years and 1 day into the past.
      val result = formWithValidDefaults(
        dayOfDispose = invalidDod.toString("dd"),
        monthOfDispose = invalidDod.toString("MM"),
        yearOfDispose = invalidDod.toString("YYYY"))

      result.errors should have length 1
      result.errors.head.key should equal(DateOfDisposalId)
      result.errors.head.message should equal("error.date.notBefore")
    }

    "reject if date is invalid (incorrect year format)" in new WithApplication {
      val yearOfDispose = "1" // should be YYYY format
      val dateServiceStubbed = dateServiceStub(yearToday = 1)

      // Attempting to dispose with an invalid date
      val result = formWithValidDefaults(yearOfDispose = yearOfDispose,
        disposeController = dispose(dateServiceStubbed))

      result.errors should have length 1
      result.errors.head.key should equal(DateOfDisposalId)
      result.errors.head.message should equal("error.date.invalid")
    }

    "reject if date entered is an invalid Gregorian date" in new WithApplication {
      val day = "31"
      val month = "02"
      val year = DateOfDisposalYearValid

      // Attempting to dispose with an invalid date.
      val result = formWithValidDefaults(
        dayOfDispose = day,
        monthOfDispose = month,
        yearOfDispose = year)

      result.errors should have length 1
      result.errors.head.key should equal(DateOfDisposalId)
      result.errors.head.message should equal("error.date.invalid")
    }
  }

  "consent" should {
    "reject if consent is not ticked" in new WithApplication {
      formWithValidDefaults(consent = "").errors should have length 1
    }
  }

  "lossOfRegistrationConsent" should {
    "reject if loss of registration consent is not ticked" in new WithApplication {
      formWithValidDefaults(lossOfRegistrationConsent = "").errors should have length 1
    }
  }

  private def dateServiceStub(dayToday: Int = DateOfDisposalDayValid.toInt,
                              monthToday: Int = DateOfDisposalMonthValid.toInt,
                              yearToday: Int = DateOfDisposalYearValid.toInt) = {
    val dayMonthYearStub = new DayMonthYear(day = dayToday,
      month = monthToday,
      year = yearToday)
    val dateService = mock[DateService]
    when(dateService.today).thenReturn(dayMonthYearStub)
    when(dateService.now).thenReturn(new Instant())
    dateService
  }

  private def dispose(dateService: DateService = dateServiceStub()) = {
    val ws = mock[DisposeWebService]
    when(ws.callDisposeService(any[DisposeRequestDto], any[TrackingId])).thenReturn(Future.successful {
      val responseAsJson = Json.toJson(disposeResponseSuccess)
      import play.api.http.Status.OK
      // Any call to a webservice will always return this successful response.
      new FakeResponse(status = OK, fakeJson = Some(responseAsJson))
    })
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] =
        invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val disposeServiceImpl = new DisposeServiceImpl(new DisposeConfig(), ws, healthStatsMock, dateServiceStub())
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])

    val emailServiceMock: EmailService = mock[EmailService]
    when(emailServiceMock.invoke(any[EmailServiceSendRequest](), any[TrackingId]))
      .thenReturn(Future(EmailServiceSendResponse()))

    implicit val config: Config = mock[Config]
    new Dispose(disposeServiceImpl, emailServiceMock, dateService, healthStatsMock)
  }

  private def formWithValidDefaults(mileage: String = MileageValid,
                                    dayOfDispose: String = DateOfDisposalDayValid,
                                    monthOfDispose: String = DateOfDisposalMonthValid,
                                    yearOfDispose: String = DateOfDisposalYearValid,
                                    consent: String = ConsentValid,
                                    lossOfRegistrationConsent: String = ConsentValid,
                                    disposeController: Dispose = dispose()) = {

    disposeController.form.bind(
      Map(
        MileageId -> mileage,
        s"$DateOfDisposalId.$DayId" -> dayOfDispose,
        s"$DateOfDisposalId.$MonthId" -> monthOfDispose,
        s"$DateOfDisposalId.$YearId" -> yearOfDispose,
        s"$EmailOptionId" -> OptionalToggle.Invisible,
        ConsentId -> consent,
        LossOfRegistrationConsentId -> lossOfRegistrationConsent
      )
    )
  }
}
