package controllers

import Common.PrototypeHtml
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs.TrackingIdValue
import helpers.{UnitSpec, TestWithApplication}
import models.DisposeFormModel.DisposeFormModelCacheKey
import models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
import models.DisposeFormModel.DisposeFormTimestampIdCacheKey
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import models.DisposeFormModelBase.Form.ConsentId
import models.DisposeFormModelBase.Form.DateOfDisposalId
import models.DisposeFormModelBase.Form.LossOfRegistrationConsentId
import models.DisposeFormModelBase.Form.MileageId
import models.DisposeFormModelBase.Form.EmailOptionId
import models.DisposeFormModelBase.Form.EmailId
import play.api.mvc.AnyContentAsFormUrlEncoded
import org.joda.time.{DateTime, DateTimeZone, Instant, LocalDate}
import org.mockito.ArgumentCaptor
import org.mockito.invocation.InvocationOnMock
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.stubbing.Answer
import pages.disposal_of_vehicle.DisposeFailurePage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.DuplicateDisposalErrorPage
import pages.disposal_of_vehicle.MicroServiceErrorPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.FORBIDDEN
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.SERVICE_UNAVAILABLE
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId => EmailEnterId, EmailVerifyId}
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.LineMaxLength
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.dispose.DisposalAddressDto
import webserviceclients.dispose.DisposalAddressDto.BuildingNameOrNumberHolder
import webserviceclients.dispose.DisposeRequestDto
import webserviceclients.dispose.DisposeResponseDto
import webserviceclients.dispose.DisposeService
import webserviceclients.dispose.DisposeServiceImpl
import webserviceclients.dispose.DisposeWebService
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValidWithSpace
import webserviceclients.fakes.FakeAddressLookupService.TraderBusinessNameValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.disposeResponseApplicationBeingProcessed
import webserviceclients.fakes.FakeDisposeWebServiceImpl.disposeResponseFailureWithDuplicateDisposal
import webserviceclients.fakes.FakeDisposeWebServiceImpl.disposeResponseSuccess
import webserviceclients.fakes.FakeDisposeWebServiceImpl.disposeResponseUnableToProcessApplication
import webserviceclients.fakes.FakeDisposeWebServiceImpl.disposeResponseUndefinedError
import webserviceclients.fakes.FakeDisposeWebServiceImpl.disposeResponseFurtherActionRequired
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import webserviceclients.fakes.{FakeDisposeWebServiceImpl, FakeResponse}

class DisposeUnitSpec extends UnitSpec {

  final val EmailValid = "my@email.com"

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
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setupTradeDetails page when present and previous pages have not been visited" in new TestWithApplication {
      val request = FakeRequest()
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display populated fields when cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeFormModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      val content = contentAsString(result)
      val contentWithCarriageReturnsAndSpacesRemoved = content.replaceAll("[\n\r]", "").replaceAll(emptySpace, "")
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "consent", isChecked = true)
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "lossOfRegistrationConsent", isChecked = true)

      private final val dod = LocalDate.now.minusYears(1)
      contentWithCarriageReturnsAndSpacesRemoved should include(buildDateControl("dateOfDisposal.day", dod.toString("dd")))
      contentWithCarriageReturnsAndSpacesRemoved should include(buildDateControl("dateOfDisposal.month", dod.toString("MM")))
      contentWithCarriageReturnsAndSpacesRemoved should include(buildDateControl("dateOfDisposal.year", dod.toString("YYYY")))
    }

    "display empty fields when cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      val content = contentAsString(result)
      val contentWithCarriageReturnsAndSpacesRemoved = content.replaceAll("[\n\r]", "").replaceAll(emptySpace, "")
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "consent", isChecked = false)
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "lossOfRegistrationConsent", isChecked = false)
      content should not include "selected" // No drop downs should be selected
    }

    "display prototype message when config set to true" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      implicit val config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false)
      when(config.googleAnalyticsTrackingId).thenReturn(None)
      when(config.assetsUrl).thenReturn(None)
      // Stub this config value.
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val webService: DisposeWebService = disposeWebService()
      val disposeService = new DisposeServiceImpl(config.dispose, webService, healthStatsMock, dateServiceStubbed())
      val result = disposeController(disposeWebService = webService, disposeService = disposeService).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to dispose success when a success message is returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val emailService = emailServiceMocked
      val result = disposeController(
        disposeWebService = disposeWebService(),
        emailService
      ).submitWithDateCheck(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
        verify(emailService, never).invoke(any[EmailServiceSendRequest], any[TrackingId])
      }
    }

    "redirect to dispose success when a success message is returned by the fake microservice with seller confirm email" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestWithEmail
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val emailService = emailServiceMocked
      val result = disposeController(
        disposeWebService = disposeWebService(),
        emailService
      ).submitWithDateCheck(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
        verify(emailService, times(1)).invoke(any[EmailServiceSendRequest], any[TrackingId])
      }
    }

    "redirect to micro-service error page when the ms indicates vss returned an error response for the attempted disposal" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceStatus = FORBIDDEN, disposeServiceResponse = None))
      val result = disposeFailure.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to duplicate-disposal error page when an duplicate disposal error occurs" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(
        disposeWebService = disposeWebService(
          disposeServiceStatus = FORBIDDEN,
          disposeServiceResponse = Some(disposeResponseFailureWithDuplicateDisposal)
        )
      )
      val result = disposeFailure.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DuplicateDisposalErrorPage.address))
      }
    }

    "redirect to setupTradeDetails page after the dispose button is clicked and no vehicleLookupFormModel is cached" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail().withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = disposeController(disposeWebService = disposeWebService()).submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "return a bad request and not call the dispose service when no details are entered" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val disposeService = disposeServiceMock()
      val result = disposeController(disposeWebService = disposeWebService(),
        disposeService = disposeService
      ).submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
        verify(disposeService, never()).invoke(any[DisposeRequestDto], any[TrackingId])
      }
    }

    "return a bad request and not call the dispose service when date of sale is 12 <= x < 24 months" in new TestWithApplication {
      val invalidDateOfSale = DateTime.now.minusYears(1).minusMonths(13)
      val request = buildCorrectlyPopulatedRequestNoEmail(
        day = invalidDateOfSale.toString("dd"),
        month = invalidDateOfSale.toString("MM"),
        year = invalidDateOfSale.getYear.toString
      )
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val disposeService = disposeServiceMock()
      val result = disposeController(
        disposeWebService = disposeWebService(),
        disposeService = disposeService
      ).submitWithDateCheck(request)

      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      verify(disposeService, never()).invoke(any[DisposeRequestDto], any[TrackingId])
      }
    }

    "calls DisposeService invoke with the expected DisposeRequest" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest()
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "redirect to setupTradeDetails page when form submitted with errors and previous pages have not been visited" in
      new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = disposeController(disposeWebService = disposeWebService()).submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to micro-service error page when calling webservice throws exception" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeResponseThrows = mock[(Int, Option[DisposeResponseDto])]
      val mockWebServiceThrows = mock[DisposeService]
      when(mockWebServiceThrows.invoke(any[DisposeRequestDto], any[TrackingId]))
        .thenReturn(Future.failed(new RuntimeException))

      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.googleAnalyticsTrackingId).thenReturn(None)

      val emailServiceMock: EmailService = mock[EmailService]
      when(emailServiceMock.invoke(any[EmailServiceSendRequest](), any[TrackingId]))
        .thenReturn(Future(EmailServiceSendResponse()))

      val healthStatsMock = mock[HealthStats]
      val dispose = new Dispose(mockWebServiceThrows, emailServiceMock, dateServiceStubbed(), healthStatsMock)
      val result = dispose.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to micro-service error page when service is unavailable" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceStatus = SERVICE_UNAVAILABLE, disposeServiceResponse = None))
      val result = disposeFailure.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to dispose success when applicationBeingProcessed" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeSuccess = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseApplicationBeingProcessed)))
      val result = disposeSuccess.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
      }
    }

    "redirect to dispose failure page when unableToProcessApplication" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(
          disposeServiceStatus = FORBIDDEN,
          disposeServiceResponse = Some(disposeResponseUnableToProcessApplication))
        )
      val result = disposeFailure.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeFailurePage.address))
      }
    }

    "redirect to error page when undefined error is returned" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(
          disposeServiceStatus = FORBIDDEN,
          disposeServiceResponse = Some(disposeResponseUndefinedError))
        )
      val result = disposeFailure.submitWithDateCheck(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "write cookies when a success message is returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).submitWithDateCheck(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        val found = cookies.find(_.name == DisposeFormTimestampIdCacheKey)
        found match {
          case Some(cookie) => cookie.value should include(CookieFactoryForUnitSpecs.disposeFormTimestamp().value)
          case _ => fail("Should have found cookie")
        }
        cookies.map(_.name) should contain allOf(
          DisposeFormTransactionIdCacheKey, DisposeFormModelCacheKey, DisposeFormTimestampIdCacheKey)
      }
    }

    "write cookies when applicationBeingProcessed" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeSuccess = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseApplicationBeingProcessed)))
      val result = disposeSuccess.submitWithDateCheck(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          DisposeFormTransactionIdCacheKey,
          DisposeFormRegistrationNumberCacheKey,
          DisposeFormModelCacheKey,
          DisposeFormTimestampIdCacheKey)
      }
    }

    "send a request and a trackingId" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())
      val mockDisposeService = mock[DisposeService]
      when(mockDisposeService.invoke(any(classOf[DisposeRequestDto]), any[TrackingId]))
        .thenReturn(Future[(Int, Option[DisposeResponseDto])] {
        (200, None)
      })
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.googleAnalyticsTrackingId).thenReturn(None)
      when(config.assetsUrl).thenReturn(None)

      val emailServiceMock: EmailService = mock[EmailService]
      when(emailServiceMock.invoke(any[EmailServiceSendRequest](), any[TrackingId]))
        .thenReturn(Future(EmailServiceSendResponse()))

      val healthStatsMock = mock[HealthStats]
      val dispose = new Dispose(mockDisposeService, emailServiceMock, dateServiceStubbed(), healthStatsMock)
      val result = dispose.submitWithDateCheck(request)
      whenReady(result) { r =>
        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[TrackingId])
        verify(mockDisposeService).invoke(any[DisposeRequestDto], trackingIdCaptor.capture())
        trackingIdCaptor.getValue should be(TrackingIdValue)
      }
    }

    "truncate address lines 1,2,3 and 4 up to max characters" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(
          buildingNameOrNumber = "a" * (LineMaxLength + 1),
          line2 = "b" * (LineMaxLength + 1),
          line3 = "c" * (LineMaxLength + 1),
          postTown = "d" * (LineMaxLength + 1)
        ))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, linePart2Truncated, linePart3Truncated),
        postTown = postTownTruncated
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate post town up to max characters" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(postTown = postTownTooLong.get))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid),
        postTown = postTownTruncated
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "remove spaces from postcode on submit" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        // postcode contains space
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(traderPostcode = PostcodeValidWithSpace))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number and place remainder on line 2 when line 2 is empty" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(buildingNameOrNumber = linePart1TooLong, line2 = ""))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", Line3Valid)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate address line 2 and place remainder on line 3 when line 3 is empty" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(line2 = linePart2TooLong, line3 = ""))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, linePart2Truncated, "b")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number and place remainder on line 2 when line 3 is empty. " +
      "Line 2 is over max length, should be placed on line 3 and truncated" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(
        buildingNameOrNumber = linePart1TooLong,
        line2 = linePart2TooLong,
        line3 = ""
      ))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", linePart2Truncated)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number when over 30 characters. Move line 2 to line 3 and remainder of " +
      "building name or number to line 2 when line 3 is empty" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(buildingNameOrNumber = linePart1TooLong, line3 = ""))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", Line2Valid)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number, create line 2 and move remainder to line2 when only " +
      "building name or number, town and postcode returned" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModelBuildingNameOrNumber(buildingNameOrNumber = linePart1TooLong))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate address line 2, create line 3 and move remainder to line3 when only " +
      "building name or number, line2, town and postcode returned" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModelLine2(line2 = linePart2TooLong))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, linePart2Truncated, "b")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number, create line 3, move line 2 to line 3 remainder of " +
      "building name or number to line 2 when building name or number, line2, " +
      "town and postcode returned" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModelLine2(
        buildingNameOrNumber = linePart1TooLong, line2 = linePart2TooLong))
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", linePart2Truncated)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "create dummy building name or number when only town and postcode returned" in new TestWithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModelPostTown())
        .withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submitWithDateCheck(request)
      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberHolder)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "redirect to vehicle lookup when a dispose success cookie exists" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequestNoEmail()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.preventGoingToDisposePage())

      val result = disposeController(disposeWebService = disposeWebService()).submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "send two internal emails when further action required" in new TestWithApplication {
      verifyEmail(buildCorrectlyPopulatedRequestNoEmail(),
        disposeResponse = disposeResponseFurtherActionRequired,
        traderEmail = None,
        expected = times(2)
      )
    }

    "not send any confirmation of sale email" in new TestWithApplication {
      verifyEmail(req = buildCorrectlyPopulatedRequestNoEmail(),
        traderEmail = None,
        expected = never()
      )
    }

    "send confirmation of sale email to trader" in new TestWithApplication {
       verifyEmail(req = buildCorrectlyPopulatedRequestNoEmail(),
         traderEmail = Some(EmailValid),
         expected = times(1)
       )
    }

    "send confirmation email to seller" in new TestWithApplication {
       verifyEmail(req = buildCorrectlyPopulatedRequestWithEmail,
         traderEmail = None,
         expected = times(1)
       )
    }

    "send confirmation email to trader and seller" in new TestWithApplication {
       verifyEmail(req = buildCorrectlyPopulatedRequestWithEmail,
         traderEmail = Some(EmailValid),
         expected = times(2)
       )
    }
  }

  private val dateValidUTC: String = new DateTime(
    DateOfDisposalYearValid.toInt,
    DateOfDisposalMonthValid.toInt,
    DateOfDisposalDayValid.toInt,
    0,
    0,
    DateTimeZone.UTC
  ).toString

  private val dateValid: String = DayMonthYear(
    DateOfDisposalDayValid.toInt,
    DateOfDisposalMonthValid.toInt,
    DateOfDisposalYearValid.toInt
  ).toDateTime.get.toString

  private val emptySpace = " "

  private def dateServiceStubbed(day: Int = DateOfDisposalDayValid.toInt,
                                 month: Int = DateOfDisposalMonthValid.toInt,
                                 year: Int = DateOfDisposalYearValid.toInt) = {
    val dateService = mock[DateService]

    val instant = new DayMonthYear(day, month, year)

    when(dateService.today).thenReturn(instant)

    when(dateService.now).thenReturn(new Instant(instant.toDateTime.get.getMillis))

    dateService
  }

  private def buildCorrectlyPopulatedRequestNoEmail(
    day: String = DateOfDisposalDayValid,
    month: String = DateOfDisposalMonthValid,
    year: String = DateOfDisposalYearValid) = {
    import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}
    FakeRequest().withFormUrlEncodedBody(
      MileageId -> MileageValid,
      s"$DateOfDisposalId.$DayId" -> day,
      s"$DateOfDisposalId.$MonthId" -> month,
      s"$DateOfDisposalId.$YearId" -> year,
      s"$EmailOptionId" -> OptionalToggle.Invisible,
      ConsentId -> FakeDisposeWebServiceImpl.ConsentValid,
      LossOfRegistrationConsentId -> FakeDisposeWebServiceImpl.ConsentValid
    )
  }

  private val buildCorrectlyPopulatedRequestWithEmail = {
    import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}
    FakeRequest().withFormUrlEncodedBody(
      MileageId -> MileageValid,
      s"$DateOfDisposalId.$DayId" -> DateOfDisposalDayValid,
      s"$DateOfDisposalId.$MonthId" -> DateOfDisposalMonthValid,
      s"$DateOfDisposalId.$YearId" -> DateOfDisposalYearValid,
      s"$EmailOptionId" -> OptionalToggle.Visible,
      s"$EmailId.$EmailEnterId" -> EmailValid,
      s"$EmailId.$EmailVerifyId" -> EmailValid,
      ConsentId -> FakeDisposeWebServiceImpl.ConsentValid,
      LossOfRegistrationConsentId -> FakeDisposeWebServiceImpl.ConsentValid
    )
  }

  private def disposeServiceMock(): DisposeService = {
    val disposeServiceMock = mock[DisposeService]
    when(disposeServiceMock.invoke(any[DisposeRequestDto], any[TrackingId]))
      .thenReturn(Future.successful((0, None)))
    disposeServiceMock
  }

  private def disposeWebService(disposeServiceStatus: Int = OK,
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

  private def emailServiceMocked: EmailService = {
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
    when(config.emailConfiguration).thenReturn(emailConfiguration)

    config
  }

  private def disposeController(disposeWebService: DisposeWebService): Dispose = {
    val disposeService = new DisposeServiceImpl(config.dispose,
      disposeWebService,
      healthStatsMock,
      dateServiceStubbed()
    )
    disposeController(disposeWebService, disposeService)
  }

  private def disposeController(disposeWebService: DisposeWebService,
                                disposeService: DisposeService,
                                emailSvc: EmailService = emailServiceMocked)
                               (implicit config: Config = config): Dispose = {
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])

    val healthStatsMock = mock[HealthStats]

    new Dispose(disposeService, emailSvc, dateServiceStubbed(), healthStatsMock)
  }

  private def disposeController(disposeWebService: DisposeWebService,
                                emailSvc: EmailService): Dispose = {
    val disposeService = new DisposeServiceImpl(config.dispose,
      disposeWebService,
      healthStatsMock,
      dateServiceStubbed()
    )
    disposeController(disposeWebService, disposeService, emailSvc)
  }

  private def checkboxHasAttributes(content: String, widgetName: String, isChecked: Boolean) = {
    val checkboxRegex = s"""<inputid="$widgetName"name="$widgetName"[^>]*>""".r
    val checkboxHtml = checkboxRegex.findFirstIn(content)
    checkboxHtml match {
      case Some(checkbox) =>
        if (isChecked) checkbox should include( """checked""")
        else {}
      case _ => fail("did not find the checkbox in ")
    }
  }

  private def buildDateControl(name: String, value: String): String = {
    s"""name="$name"value="$value""""
  }

  private val linePart1Truncated: String = "a" * LineMaxLength
  private val linePart2Truncated: String = "b" * LineMaxLength
  private val linePart3Truncated: String = "c" * LineMaxLength
  private val linePart1TooLong: String = linePart1Truncated + "a"
  private val linePart2TooLong: String = linePart2Truncated + "b"
  private val postTownTruncated: Option[String] = Some("d" * LineMaxLength)
  private val postTownTooLong: Option[String] = Some("d" * (LineMaxLength + 1))

  private def expectedDisposeRequest(referenceNumber: String = ReferenceNumberValid,
                                     registrationNumber: String = RegistrationNumberValid,
                                     traderName: String = TraderBusinessNameValid,
                                     line: Seq[String] = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid),
                                     postTown: Option[String] = Some(PostTownValid),
                                     postCode: String = PostcodeValid,
                                     dateOfDisposal: String = dateValidUTC,
                                     transactionTimestamp: String = dateValid,
                                     prConsent: Boolean = FakeDisposeWebServiceImpl.ConsentValid.toBoolean,
                                     keeperConsent: Boolean = FakeDisposeWebServiceImpl.ConsentValid.toBoolean,
                                     mileage: Option[Int] = Some(MileageValid.toInt)) =

    DisposeRequestDto(
      VssWebHeaderDto(
        "trackingId",
        new org.joda.time.DateTime(transactionTimestamp),
        config.applicationCode,
        config.vssServiceTypeCode,
        VssWebEndUserDto(config.orgBusinessUnit, traderName)
      ),
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      traderName = traderName,
      traderAddress = DisposalAddressDto(
        line = line,
        postTown = postTown,
        postCode = postCode
      ),
      dateOfDisposal = dateOfDisposal,
      transactionTimestamp = transactionTimestamp,
      prConsent = prConsent,
      keeperConsent = keeperConsent,
      mileage = mileage
    )

  private def verifyEmail(req: FakeRequest[AnyContentAsFormUrlEncoded],
                          disposeResponse: DisposeResponseDto = disposeResponseSuccess,
                          traderEmail: Option[String],
                          expected: org.mockito.verification.VerificationMode) {

    val request = req
      .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(traderEmail = traderEmail))
      .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())

    val disposeWebServiceMock =  disposeWebService()
    when(disposeWebServiceMock.callDisposeService(any[DisposeRequestDto], any[TrackingId]))
      .thenReturn(Future.successful {
        val responseAsJson = Json.toJson(disposeResponse)
        new FakeResponse(status = OK, fakeJson = Some(responseAsJson))
        })

    val emailServiceMock = emailServiceMocked

    val disposeSuccess = disposeController(
      disposeWebService = disposeWebServiceMock,
      emailServiceMock
    )

    val result = disposeSuccess.submitWithDateCheck(request)
    whenReady(result) { r =>
      r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
      verify(emailServiceMock, expected).invoke(any[EmailServiceSendRequest], any[TrackingId])
    }
  }
}
