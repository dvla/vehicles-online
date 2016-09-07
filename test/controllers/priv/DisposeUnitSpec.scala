package controllers.priv

import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, TestWithApplication}
import models.DisposeFormModelBase.Form.ConsentId
import models.DisposeFormModelBase.Form.DateOfDisposalId
import models.DisposeFormModelBase.Form.LossOfRegistrationConsentId
import models.DisposeFormModelBase.Form.MileageId
import models.PrivateDisposeFormModel.DisposeFormTimestampIdCacheKey
import models.PrivateDisposeFormModel.DisposeFormTransactionIdCacheKey
import models.PrivateDisposeFormModel.PrivateDisposeFormModelCacheKey
import models.DisposeFormModelBase.Form.EmailOptionId
import org.joda.time.Instant
import org.mockito.invocation.InvocationOnMock
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.stubbing.Answer
import pages.disposal_of_vehicle.DisposeSuccessForPrivateKeeperPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import common.mappings.OptionalToggle
import common.mappings.Email.{EmailId, EmailVerifyId}
import common.services.DateService
import common.services.SEND.EmailConfiguration
import common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import common.views.models.DayMonthYear
import common.webserviceclients.emailservice.EmailService
import common.webserviceclients.emailservice.EmailServiceSendRequest
import common.webserviceclients.emailservice.EmailServiceSendResponse
import common.webserviceclients.emailservice.From
import common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.dispose.DisposeRequestDto
import webserviceclients.dispose.DisposeResponseDto
import webserviceclients.dispose.DisposeService
import webserviceclients.dispose.DisposeServiceImpl
import webserviceclients.dispose.DisposeWebService
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.disposeResponseSuccess
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageValid
import webserviceclients.fakes.{FakeDisposeWebServiceImpl, FakeResponse}

class DisposeUnitSpec extends UnitSpec {

  val healthStatsMock = mock[HealthStats]
  when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
    override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
  })

  "present" should {
    "display the page" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = disposeController(disposeWebService = disposeWebServiceMock()).present(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }
  }

  "submit" should {
    "redirect to dispose success for private keeper, with confirmation email, when " +
      "a success message is returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val emailService = emailServiceMock
      val result = disposeController(
        disposeWebService = disposeWebServiceMock(),
        emailService = emailService
      ).submitWithDateCheck(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessForPrivateKeeperPage.address))
        verify(emailService, times(1)).invoke(any[EmailServiceSendRequest], any[TrackingId])
      }
    }

    "redirect to dispose success for private keeper, with no confirmation email, when " +
      "a success message is returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val emailService = emailServiceMock
      val result = disposeController(
        disposeWebService = disposeWebServiceMock(),
        emailService = emailService
      ).submitWithDateCheck(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessForPrivateKeeperPage.address))
        verify(emailService, never).invoke(any[EmailServiceSendRequest], any[TrackingId])
      }
    }

    "write cookies when a success message is returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeController(disposeWebService = disposeWebServiceMock()).submitWithDateCheck(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        val found = cookies.find(_.name == DisposeFormTimestampIdCacheKey)
        found match {
          case Some(cookie) => cookie.value should include(CookieFactoryForUnitSpecs.disposeFormTimestamp().value)
          case _ => fail("Should have found cookie")
        }
        cookies.map(_.name) should contain allOf(
          DisposeFormTransactionIdCacheKey, PrivateDisposeFormModelCacheKey, DisposeFormTimestampIdCacheKey)
      }
    }
  }

  private def dateServiceStubbed(day: Int = DateOfDisposalDayValid.toInt,
                                 month: Int = DateOfDisposalMonthValid.toInt,
                                 year: Int = DateOfDisposalYearValid.toInt) = {
    val dateService = mock[DateService]
    when(dateService.today).thenReturn(new DayMonthYear(day = day,
      month = month,
      year = year))

    val instant = new DayMonthYear(day = day,
      month = month,
      year = year).toDateTime.get.getMillis

    when(dateService.now).thenReturn(new Instant(instant))
    dateService
  }

  private val buildCorrectlyPopulatedRequest = {
    import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear._
    FakeRequest().withFormUrlEncodedBody(
      MileageId -> MileageValid,
      s"$DateOfDisposalId.$DayId" -> DateOfDisposalDayValid,
      s"$DateOfDisposalId.$MonthId" -> DateOfDisposalMonthValid,
      s"$DateOfDisposalId.$YearId" -> DateOfDisposalYearValid,
      s"$EmailOptionId" -> OptionalToggle.Visible,
      s"$EmailId.$EmailId" -> "my@email.com",
      s"$EmailId.$EmailVerifyId" -> "my@email.com",
      ConsentId -> FakeDisposeWebServiceImpl.ConsentValid,
      LossOfRegistrationConsentId -> FakeDisposeWebServiceImpl.ConsentValid
    )
  }

  private val buildCorrectlyPopulatedRequestNoEmail = {
    import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear._
    FakeRequest().withFormUrlEncodedBody(
      MileageId -> MileageValid,
      s"$DateOfDisposalId.$DayId" -> DateOfDisposalDayValid,
      s"$DateOfDisposalId.$MonthId" -> DateOfDisposalMonthValid,
      s"$DateOfDisposalId.$YearId" -> DateOfDisposalYearValid,
      s"$EmailOptionId" -> OptionalToggle.Invisible,
      ConsentId -> FakeDisposeWebServiceImpl.ConsentValid,
      LossOfRegistrationConsentId -> FakeDisposeWebServiceImpl.ConsentValid
    )
  }

  private def disposeWebServiceMock(disposeServiceStatus: Int = OK,
                                disposeServiceResponse: Option[DisposeResponseDto] = Some(disposeResponseSuccess)
                                 ): DisposeWebService = {
    val disposeWebService = mock[DisposeWebService]
    when(disposeWebService.callDisposeService(any[DisposeRequestDto], any[TrackingId]))
      .thenReturn(Future.successful {
      val fakeJson = disposeServiceResponse map (Json.toJson(_))
      // Any call to a webservice will always return this successful response.
      new FakeResponse(status = disposeServiceStatus, fakeJson = fakeJson)
    })
    disposeWebService
  }

  private def disposeServiceMock(disposeWebService: DisposeWebService): DisposeService =
    new DisposeServiceImpl(config.disposeConfig, disposeWebService, healthStatsMock, dateServiceStubbed()
  )

  private def emailServiceMock: EmailService = {
    val emailServiceMock: EmailService = mock[EmailService]
    when(emailServiceMock.invoke(any[EmailServiceSendRequest](), any[TrackingId]))
      .thenReturn(Future(EmailServiceSendResponse()))
    emailServiceMock
  }

  private val config: Config = {
    val config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(true)
    when(config.googleAnalyticsTrackingId).thenReturn(None)
    when(config.assetsUrl).thenReturn(None)

    val emailConfiguration = EmailConfiguration(
      from = From(email = "", name = ""),
      feedbackEmail = From(email = "", name = ""),
      whiteList = None
    )
    when(config.emailConfiguration). thenReturn(emailConfiguration)
    config
  }

  private def disposeController(disposeWebService: DisposeWebService): Dispose =
    disposeController(disposeWebService, disposeServiceMock(disposeWebService), emailServiceMock)

  private def disposeController(disposeWebService: DisposeWebService,
                                emailService: EmailService): Dispose =
    disposeController(disposeWebService, disposeServiceMock(disposeWebService), emailService)

  private def disposeController(disposeWebService: DisposeWebService,
                                disposeService: DisposeService,
                                emailService: EmailService)
                               (implicit config: Config = config): Dispose = {
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])

    new Dispose(disposeService, emailService, dateServiceStubbed(), healthStatsMock)
  }
}
