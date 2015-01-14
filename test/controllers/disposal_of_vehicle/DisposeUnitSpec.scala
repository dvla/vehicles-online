package controllers.disposal_of_vehicle

import controllers.Dispose
import controllers.disposal_of_vehicle.Common.PrototypeHtml
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs.TrackingIdValue
import helpers.{UnitSpec, WithApplication}
import models.DisposeFormModel.Form.{ConsentId, DateOfDisposalId, LossOfRegistrationConsentId, MileageId}
import models.DisposeFormModel.{DisposeFormModelCacheKey, DisposeFormRegistrationNumberCacheKey, DisposeFormTimestampIdCacheKey, DisposeFormTransactionIdCacheKey}
import org.joda.time.Instant
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.disposal_of_vehicle.{DisposeFailurePage, DisposeSuccessPage, DuplicateDisposalErrorPage, MicroServiceErrorPage, SetupTradeDetailsPage, VehicleLookupPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, INTERNAL_SERVER_ERROR, LOCATION, OK, SERVICE_UNAVAILABLE, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.LineMaxLength
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import utils.helpers.Config
import webserviceclients.dispose.DisposalAddressDto.BuildingNameOrNumberHolder
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeResponseDto, DisposeService, DisposeServiceImpl, DisposeWebService}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, PostcodeValid, PostcodeValidWithSpace, TraderBusinessNameValid}
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import webserviceclients.fakes.FakeDisposeWebServiceImpl.{MileageValid, disposeResponseApplicationBeingProcessed, disposeResponseFailureWithDuplicateDisposal, disposeResponseSuccess, disposeResponseUnableToProcessApplication, disposeResponseUndefinedError}
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import webserviceclients.fakes.{FakeDisposeWebServiceImpl, FakeResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class DisposeUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setupTradeDetails page when present and previous pages have not been visited" in new WithApplication {
      val request = FakeRequest()
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      val content = contentAsString(result)
      val contentWithCarriageReturnsAndSpacesRemoved = content.replaceAll("[\n\r]", "").replaceAll(emptySpace, "")
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "consent", isChecked = true, isAutoFocus = true)
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "lossOfRegistrationConsent", isChecked = true, isAutoFocus = false)

      contentWithCarriageReturnsAndSpacesRemoved should include(buildSelectedOptionHtml("25", "25"))
      contentWithCarriageReturnsAndSpacesRemoved should include(buildSelectedOptionHtml("11", "November"))
      contentWithCarriageReturnsAndSpacesRemoved should include(buildSelectedOptionHtml("1970", "1970"))
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      val content = contentAsString(result)
      val contentWithCarriageReturnsAndSpacesRemoved = content.replaceAll("[\n\r]", "").replaceAll(emptySpace, "")
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "consent", isChecked = false, isAutoFocus = true)
      checkboxHasAttributes(contentWithCarriageReturnsAndSpacesRemoved, "lossOfRegistrationConsent", isChecked = false, isAutoFocus = false)
      content should not include "selected" // No drop downs should be selected
    }

    "display prototype message when config set to true" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      implicit val config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false)
      // Stub this config value.
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val webService: DisposeWebService = disposeWebService()
      val disposeService = new DisposeServiceImpl(config.dispose, webService)
      val result = disposeController(disposeWebService = webService, disposeService = disposeService).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to dispose success when a success message is returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val result = disposeController(disposeWebService = disposeWebService()).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
      }
    }

    "redirect to micro-service error page when an unexpected error occurs" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceStatus = INTERNAL_SERVER_ERROR, disposeServiceResponse = None))
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to duplicate-disposal error page when an duplicate disposal error occurs" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(
        disposeWebService = disposeWebService(
          disposeServiceStatus = OK,
          disposeServiceResponse = Some(disposeResponseFailureWithDuplicateDisposal)
        )
      )
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DuplicateDisposalErrorPage.address))
      }
    }

    "redirect to setupTradeDetails page after the dispose button is clicked and no vehicleLookupFormModel is cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "return a bad request when no details are entered" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to setupTradeDetails page when form submitted with errors and previous pages have not been visited" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to micro-service error page when calling webservice throws exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeResponseThrows = mock[(Int, Option[DisposeResponseDto])]
      val mockWebServiceThrows = mock[DisposeService]
      when(mockWebServiceThrows.invoke(any[DisposeRequestDto], any[String])).thenReturn(Future.failed(new RuntimeException))
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      val dispose = new Dispose(mockWebServiceThrows, dateServiceStubbed())
      val result = dispose.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to micro-service error page when service is unavailable" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceStatus = SERVICE_UNAVAILABLE, disposeServiceResponse = None))
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to dispose success when applicationBeingProcessed" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeSuccess = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseApplicationBeingProcessed)))
      val result = disposeSuccess.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
      }
    }

    "redirect to dispose failure page when unableToProcessApplication" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseUnableToProcessApplication)))
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeFailurePage.address))
      }
    }

    "redirect to error page when undefined error is returned" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseUndefinedError)))
      val result = disposeFailure.submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "write cookies when a success message is returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
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

    "write cookies when applicationBeingProcessed" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeSuccess = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseApplicationBeingProcessed)))
      val result = disposeSuccess.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          DisposeFormTransactionIdCacheKey,
          DisposeFormRegistrationNumberCacheKey,
          DisposeFormModelCacheKey,
          DisposeFormTimestampIdCacheKey)
      }
    }

    "calls DisposeService invoke with the expected DisposeRequest" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest()

      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "send a request and a trackingId" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())
      val mockDisposeService = mock[DisposeService]
      when(mockDisposeService.invoke(any(classOf[DisposeRequestDto]), any[String])).
        thenReturn(Future[(Int, Option[DisposeResponseDto])] {
        (200, None)
      })
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      val dispose = new Dispose(mockDisposeService, dateServiceStubbed())
      val result = dispose.submit(request)
      whenReady(result) { r =>
        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
        verify(mockDisposeService).invoke(any[DisposeRequestDto], trackingIdCaptor.capture())
        trackingIdCaptor.getValue should be(TrackingIdValue)
      }
    }

    "truncate address lines 1,2,3 and 4 up to max characters" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(
        buildingNameOrNumber = "a" * (LineMaxLength + 1),
        line2 = "b" * (LineMaxLength + 1),
        line3 = "c" * (LineMaxLength + 1),
        postTown = "d" * (LineMaxLength + 1)
      )).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, linePart2Truncated, linePart3Truncated),
        postTown = postTownTruncated
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate post town up to max characters" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(postTown = postTownTooLong.get)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid),
        postTown = postTownTruncated
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "remove spaces from postcode on submit" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(traderPostcode = PostcodeValidWithSpace)). // postcode contains space
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid)
      )

      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number and place remainder on line 2 when line 2 is empty" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(buildingNameOrNumber = linePart1TooLong, line2 = "")).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", Line3Valid)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }


    "truncate address line 2 and place remainder on line 3 when line 3 is empty" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(line2 = linePart2TooLong, line3 = "")).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, linePart2Truncated, "b")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number and place remainder on line 2 when line 3 is empty. Line 2 is over max length, should be placed on line 3 and truncated" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(
        buildingNameOrNumber = linePart1TooLong,
        line2 = linePart2TooLong,
        line3 = ""
      )).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", linePart2Truncated)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number when over 30 characters. Move line 2 to line 3 and remainder of building name or number to line 2 when line 3 is empty" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(buildingNameOrNumber = linePart1TooLong, line3 = "")).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", Line2Valid)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number, create line 2 and move reaminder to line2  when only building name or number, town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelBuildingNameOrNumber(
        buildingNameOrNumber = linePart1TooLong)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate address line 2, create line 3 and move reaminder to line3 when only building name or number, line2, town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelLine2(line2 = linePart2TooLong)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, linePart2Truncated, "b")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number, create line 3, move line 2 to line 3 remainder of building name or number to line 2 when building name or number, line2, town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelLine2(
        buildingNameOrNumber = linePart1TooLong, line2 = linePart2TooLong)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", linePart2Truncated)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "create dummy building name or number when only town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelPostTown()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberHolder)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "redirect to vehicle lookup when a dispose success cookie exists" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.preventGoingToDisposePage())

      val result = disposeController(disposeWebService = disposeWebService()).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

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
      ConsentId -> FakeDisposeWebServiceImpl.ConsentValid,
      LossOfRegistrationConsentId -> FakeDisposeWebServiceImpl.ConsentValid
    )
  }

  private def disposeServiceMock(): DisposeService = {
    val disposeServiceMock = mock[DisposeService]
    when(disposeServiceMock.invoke(any[DisposeRequestDto], any[String])).
      thenReturn(Future.successful((0, None)))

    disposeServiceMock
  }

  private def disposeWebService(disposeServiceStatus: Int = OK,
                                disposeServiceResponse: Option[DisposeResponseDto] = Some(disposeResponseSuccess)): DisposeWebService = {
    val disposeWebService = mock[DisposeWebService]
    when(disposeWebService.callDisposeService(any[DisposeRequestDto], any[String])).
      thenReturn(Future.successful {
      val fakeJson = disposeServiceResponse map (Json.toJson(_))
      new FakeResponse(status = disposeServiceStatus, fakeJson = fakeJson) // Any call to a webservice will always return this successful response.
    })

    disposeWebService
  }

  private val config: Config = {
    val config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(true)
    config
  }

  private def disposeController(disposeWebService: DisposeWebService): Dispose = {
    val disposeService = new DisposeServiceImpl(config.dispose, disposeWebService)
    disposeController(disposeWebService, disposeService)
  }

  private def disposeController(disposeWebService: DisposeWebService, disposeService: DisposeService)
                               (implicit config: Config = config): Dispose = {
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])

    new Dispose(disposeService, dateServiceStubbed())
  }

  private def checkboxHasAttributes(content: String, widgetName: String, isChecked: Boolean, isAutoFocus: Boolean) = {
    val checkboxRegex = s"""<inputid="$widgetName"name="$widgetName"[^>]*>""".r
    val checkboxHtml = checkboxRegex.findFirstIn(content)
    checkboxHtml match {
      case Some(checkbox) =>
        if (isChecked) checkbox should include( """checked""")
        else {}
        if (isAutoFocus) checkbox should include( """autofocus="true"""")
        else {}
      case _ => fail("did not find the checkbox in ")
    }
  }

  private def buildSelectedOptionHtml(optionValue: String, optionText: String): String = {
    s"""<optionvalue="$optionValue"selected>$optionText</option>"""
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
                                     uprn: Option[Long] = None,
                                     dateOfDisposal: String = dateValid,
                                     transactionTimestamp: String = dateValid,
                                     prConsent: Boolean = FakeDisposeWebServiceImpl.ConsentValid.toBoolean,
                                     keeperConsent: Boolean = FakeDisposeWebServiceImpl.ConsentValid.toBoolean,
                                     mileage: Option[Int] = Some(MileageValid.toInt)) =
    DisposeRequestDto(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      traderName = traderName,
      traderAddress = DisposalAddressDto(
        line = line,
        postTown = postTown,
        postCode = postCode,
        uprn = uprn
      ),
      dateOfDisposal = dateOfDisposal,
      transactionTimestamp = transactionTimestamp,
      prConsent = prConsent,
      keeperConsent = keeperConsent,
      mileage = mileage
    )
}
