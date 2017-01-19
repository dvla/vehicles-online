package controllers

import Common.PrototypeHtml
import com.tzavellas.sse.guice.ScalaModule
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, TestWithApplication}
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModel.SurveyRequestTriggerDateCacheKey
import models.VehicleLookupFormModel
import models.VehicleLookupFormModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import org.joda.time.Instant
import org.mockito.ArgumentCaptor
import org.mockito.invocation.InvocationOnMock
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.stubbing.Answer
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.DuplicateDisposalErrorPage
import pages.disposal_of_vehicle.EnterAddressManuallyPage
import pages.disposal_of_vehicle.MicroServiceErrorPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupFailurePage
import pages.disposal_of_vehicle.VrmLockedPage
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, contentAsString, defaultAwaitTimeout, LOCATION}
import scala.concurrent.duration.DurationInt
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.clientsidesession.ClientSideSessionFactory
import common.mappings.DocumentReferenceNumber
import common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import common.model.MicroserviceResponseModel.MsResponseCacheKey
import common.services.DateServiceImpl
import common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import common.testhelpers.JsonUtils.deserializeJsonToModel
import common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionServiceImpl
import common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import common.webserviceclients.healthstats.HealthStats
import common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupSuccessResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import vehicleandkeeperlookup.VehicleAndKeeperLookupServiceImpl
import vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import utils.helpers.Config
import views.disposal_of_vehicle.VehicleLookup.ResetTraderDetailsId
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import webserviceclients.fakes.FakeAddressLookupService.TraderBusinessNameValid
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.selectedAddress
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.ReferenceNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberWithSpaceValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsDisposedVehicleResponseSuccess
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsNoResponse
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseDocRefNumberNotLatest
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseNotFoundResponseCode
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseSuccess
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseUnhandledException
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseVRMNotFound
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsServerDown
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.responseFirstAttempt
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.responseSecondAttempt
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.VrmAttempt2
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.VrmLocked
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.VrmThrows
import webserviceclients.fakes.{FakeDateServiceImpl, FakeResponse}

class VehicleLookupUnitSpec extends UnitSpec {

  val healthStatsMock = mock[HealthStats]
  when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
    override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
  })

  "present" should {
    "display the page" in new TestWithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "redirect to setupTradeDetails page when user has not set up a trader for disposal" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator().present(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "display populated fields when cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(ReferenceNumberValid)
      content should include(RegistrationNumberValid)
    }

    "not display exit anchor when DisposeOccurredCacheKey cookie is missing" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should not include ExitAnchorHtml
    }

    "display exit anchor when DisposeOccurredCacheKey cookie is present" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(ExitAnchorHtml)
    }

    "display reset trade details anchor" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(s"""a id="$ResetTraderDetailsId""")
    }

    "display data captured in previous pages" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)

      content should include(TraderBusinessNameValid)
      content should include(BuildingNameOrNumberValid)
      content should include(Line2Valid)
      content should include(Line3Valid)
      content should include(PostTownValid)
      content should include(webserviceclients.fakes.FakeAddressLookupService.PostcodeValid)
    }

    "display empty fields when cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator(isPrototypeBannerVisible = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "offer the survey on first successful dispose" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)
      implicit val config = mockSurveyConfig()

      val vehiclesLookup = lookupWithMockConfig(config)
      contentAsString(vehiclesLookup.present(request)) should include(config.surveyUrl)
    }

    "not offer the survey for one just after the initial survey offering" in new TestWithApplication {
      implicit val config = mockSurveyConfig()
      val aMomentAgo = (Instant.now.getMillis - 100).toString

      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(aMomentAgo))
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)

      val vehiclesLookup = lookupWithMockConfig(config)
      contentAsString(vehiclesLookup.present(request)) should not include config.surveyUrl
    }

    "offer the survey one week after the first offering" in new TestWithApplication {
      implicit val config = mockSurveyConfig()

      val moreThen7daysAgo =
        (Instant.now.getMillis - config.prototypeSurveyPrepositionInterval - 1.minute.toMillis).toString
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(moreThen7daysAgo))
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)

      val vehiclesLookup = lookupWithMockConfig(config)
      contentAsString(vehiclesLookup.present(request)) should include(config.surveyUrl)
    }

    "not offer the survey one week after the first offering" in new TestWithApplication {
      implicit val config = mockSurveyConfig()

      val lessThen7daysАgo =
        (Instant.now.getMillis - config.prototypeSurveyPrepositionInterval + 1.minute.toMillis).toString
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(lessThen7daysАgo))
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)

      val vehiclesLookup = lookupWithMockConfig(config)
      contentAsString(vehiclesLookup.present(request)) should not include config.surveyUrl
    }

    "not offer the survey if the survey url is not set in the config" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)
      val vehiclesLookup = lookupWithMockConfig(mockSurveyConfig(""))
      val result = vehiclesLookup.present(request)
      contentAsString(result) should not include "survey"
    }
  }

  "submit" should {
    "redirect to Dispose after a valid submit and true message returned from the fake microservice" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator().submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposePage.address))
        val cookies = fetchCookiesFromHeaders(r)
        val cookieName = VehicleLookupFormModelCacheKey
        cookies.find(_.name == cookieName) match {
          case Some(cookie) =>
            val json = cookie.value
            val model = deserializeJsonToModel[VehicleLookupFormModel](json)
            model.registrationNumber should equal(RegistrationNumberValid.toUpperCase)
          case None => fail(s"$cookieName cookie not found")
        }
      }
    }

    "submit removes spaces from registrationNumber" in new TestWithApplication {
      // DE7 Spaces should be stripped
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
      val result = vehicleLookupResponseGenerator().submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(VehicleLookupFormModelCacheKey)
      }
    }

    "redirect to MicroServiceError after a submit and no response code and no vehicledetailsdto returned from " +
      "the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseNotFoundResponseCode).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "redirect to VehicleLookupFailure after a submit and vrm not found by the fake microservice" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseVRMNotFound).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to VehicleLookupFailure after a submit and document reference number mismatch returned by the " +
      "fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to VehicleLookupFailure after a submit and vss error returned by the fake microservice" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsServerDown).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "redirect to VehicleLookupFailure after a submit and unhandled exception returned by the fake microservice" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseUnhandledException).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "return a bad request if dealer details are in cache and no details are entered" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "", registrationNumber = "")
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      result.futureValue.header.status should equal(play.api.http.Status.BAD_REQUEST)
    }

    "redirect to setupTradeDetails page if dealer details are not in cache and no details are entered" in new
        TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "", registrationNumber = "")
      val result = vehicleLookupResponseGenerator().submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "replace max length error message for document reference number with standard error message (US43)" in new
        TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1))
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with " +
      "standard error message (US43)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "")
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number"
        .r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration number with standard error message (US43)" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Vehicle registration number must be valid format".r.findAllIn(contentAsString(result)).length
      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration number with standard " +
      "error message (US43)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Vehicle registration number must be valid format".r.findAllIn(contentAsString(result)).length
      // The same message is displayed in 2 places - once in the validation-summary at the top of the page
      // and once above the field.
      count should equal(2)
    }

    "redirect to EnterAddressManually when back button is pressed and the user has manually entered an address" in
      new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = vehicleLookupResponseGenerator().back(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(EnterAddressManuallyPage.address))
    }

    "redirect to BusinessChooseYourAddress when back button is pressed and there is a uprn" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().back(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to BusinessChooseYourAddress when back is called and the user has completed the vehicle lookup form" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddressUseAddress())
      val result = vehicleLookupResponseGenerator().back(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to MicroserviceError when microservice throws" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupError.submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError after a submit if response status is Ok and no response payload" in
      new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsNoResponse).submit(request)

      // TODO This test passes for the wrong reason, it is throwing when VehicleLookupServiceImpl tries to access
      // resp.json, whereas we want VehicleLookupServiceImpl to return None as a response payload.
      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "write cookie when vss error returned by the microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsServerDown).submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(VehicleLookupFormModelCacheKey)
      }
    }

    "write cookie when document reference number mismatch returned by microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(fullResponse = vehicleDetailsResponseDocRefNumberNotLatest)
        .submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          bruteForcePreventionViewModelCacheKey, MsResponseCacheKey, VehicleLookupFormModelCacheKey)
      }
    }

    "write cookie when vrm not found by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(fullResponse = vehicleDetailsResponseVRMNotFound).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          bruteForcePreventionViewModelCacheKey, MsResponseCacheKey, VehicleLookupFormModelCacheKey)
      }
    }

    "redirect to vrm locked when valid submit and brute force prevention returns not permitted" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmLocked)
      val result = vehicleLookupResponseGenerator(
        vehicleDetailsResponseDocRefNumberNotLatest,
        bruteForceService = bruteForceServiceImpl(permitted = false)
      ).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
    }

    "redirect to VehicleLookupFailure and display 1st attempt message when document reference number " +
     "not found and security service returns 1st attempt" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberValid)
      val result = vehicleLookupResponseGenerator(
        vehicleDetailsResponseDocRefNumberNotLatest,
        bruteForceService = bruteForceServiceImpl(permitted = true)
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to VehicleLookupFailure and display 2nd attempt message when document reference number " +
      "not found and security service returns 2nd attempt" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmAttempt2)
      val result = vehicleLookupResponseGenerator(
        vehicleDetailsResponseDocRefNumberNotLatest,
        bruteForceService = bruteForceServiceImpl(permitted = true)
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to DuplicateDisposalError if the keeper end date is present for that vehicle" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(
        vehicleDetailsDisposedVehicleResponseSuccess,
        bruteForceService = bruteForceServiceImpl(permitted = true)
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(DuplicateDisposalErrorPage.address))
    }

    "Send a request and a trackingId" in new TestWithApplication {
      val trackingId = TrackingId("default_test_tracking_id")
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))

      val (vehiclesLookup, mockVehiclesLookupService) = vehicleLookupControllerAndMocks()
      when(mockVehiclesLookupService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
        .thenReturn(Future.successful {
        new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get.right.get)))
      })

      val result = vehiclesLookup.submit(request)
      whenReady(result) { r =>
        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[TrackingId])
        verify(mockVehiclesLookupService).invoke(any[VehicleAndKeeperLookupRequest], trackingIdCaptor.capture())
        trackingIdCaptor.getValue should be(trackingId)
      }
    }

    "Send the request and no trackingId if session is not present" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val (vehiclesLookup, mockVehiclesLookupService) = vehicleLookupControllerAndMocks()
      when(mockVehiclesLookupService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
        .thenReturn(Future.successful {
        new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get.right.get)))
      })

      val result = vehiclesLookup.submit(request)
      whenReady(result) { r =>
        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[TrackingId])
        verify(mockVehiclesLookupService).invoke(any[VehicleAndKeeperLookupRequest], trackingIdCaptor.capture())
        trackingIdCaptor.getValue should be(ClearTextClientSideSessionFactory.DefaultTrackingId)
      }
    }

    "call the vehicle lookup micro service and brute force service after a valid request" in new TestWithApplication {
      val (bruteForceService, bruteForceWebServiceMock) = bruteForceServiceAndWebServiceMock(permitted = true)
      val (vehicleLookupController, vehicleLookupMicroServiceMock) = vehicleLookupControllerAndMocks(
        bruteForceService = bruteForceService
      )
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupController.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposePage.address))
        verify(bruteForceWebServiceMock, times(1)).callBruteForce(anyString(), any[TrackingId])
        verify(vehicleLookupMicroServiceMock, times(1)).invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId])
      }
    }

    "not call the vehicle lookup micro service after a invalid request" in new TestWithApplication {
      val (bruteForceService, bruteForceWebServiceMock) = bruteForceServiceAndWebServiceMock(permitted = true)
      val (vehicleLookupController, vehicleLookupMicroServiceMock) = vehicleLookupControllerAndMocks(
        bruteForceService = bruteForceService
      )
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupController.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
        verify(bruteForceWebServiceMock, never()).callBruteForce(anyString(), any[TrackingId])
        verify(vehicleLookupMicroServiceMock, never()).invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId])
      }
    }
  }

  "exit" should {
    "redirect to end page from configuration" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/sell-to-the-trade/before-you-start"))
      }
    }

    "set the surveyRequestTriggerDate to the current date" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.preventGoingToDisposePage(""))
      val result = lookupWithMockConfig(mockSurveyConfig("http://www.google.com")).exit(request)
      whenReady(result) {r =>
        val cookies = fetchCookiesFromHeaders(r)
        val surveyTime = cookies.find(_.name == SurveyRequestTriggerDateCacheKey).get.value.toLong
        surveyTime should be <= System.currentTimeMillis()
        surveyTime should be > System.currentTimeMillis() - 1000
      }
    }
  }

  private val ExitAnchorHtml = """a id="exit""""

  private def responseThrows: Future[WSResponse] = Future.failed(
    new RuntimeException("This error is generated deliberately by a test")
  )

  private def bruteForceServiceImpl(permitted: Boolean): BruteForcePreventionService = {
    val (bruteForcePreventionService, _) = bruteForceServiceAndWebServiceMock(permitted)
    bruteForcePreventionService
  }

  private def bruteForceServiceAndWebServiceMock(permitted: Boolean): (BruteForcePreventionService,
    BruteForcePreventionWebService
    ) = {

    def bruteForcePreventionWebService: BruteForcePreventionWebService = {
      val status = if (permitted) play.api.http.Status.OK else play.api.http.Status.FORBIDDEN
      val bruteForcePreventionWebService: BruteForcePreventionWebService = mock[BruteForcePreventionWebService]

      when(bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid,
        TrackingId("default_test_tracking_id"))
      ).thenReturn(Future.successful(new FakeResponse(status = status, fakeJson = responseFirstAttempt)))

      when(bruteForcePreventionWebService.callBruteForce(FakeBruteForcePreventionWebServiceImpl.VrmAttempt2,
        TrackingId("default_test_tracking_id"))
      ).thenReturn(Future.successful(new FakeResponse(status = status, fakeJson = responseSecondAttempt)))

      when(bruteForcePreventionWebService.callBruteForce(FakeBruteForcePreventionWebServiceImpl.VrmLocked,
        TrackingId("default_test_tracking_id"))
      ).thenReturn(Future.successful(new FakeResponse(status = status)))

      when(bruteForcePreventionWebService.callBruteForce(VrmThrows,
        TrackingId("default_test_tracking_id"))
      ).thenReturn(responseThrows)

      when(bruteForcePreventionWebService.reset(any[String], any[TrackingId]))
        .thenReturn(Future.successful(new FakeResponse(status = play.api.http.Status.OK)))

      bruteForcePreventionWebService
    }

    val bruteForcePreventionWebServiceMock = bruteForcePreventionWebService
    val bruteForcePreventionService = new BruteForcePreventionServiceImpl(
      config = new BruteForcePreventionConfig,
      ws = bruteForcePreventionWebServiceMock,
      healthStatsMock,
      dateService = new FakeDateServiceImpl
    )
    (bruteForcePreventionService, bruteForcePreventionWebServiceMock)
  }

  private def vehicleLookupResponseGenerator(fullResponse:
                                             (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                 VehicleAndKeeperLookupSuccessResponse]]) = vehicleDetailsResponseSuccess,
                                              bruteForceService: BruteForcePreventionService = bruteForceServiceImpl(permitted = true),
                                              isPrototypeBannerVisible: Boolean = true): VehicleLookup = {
    val (vehicleLookupController, _) = vehicleLookupControllerAndMocks(fullResponse,
      bruteForceService,
      isPrototypeBannerVisible
    )
    vehicleLookupController
  }

  private def vehicleLookupControllerAndMocks(fullResponse: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                                VehicleAndKeeperLookupSuccessResponse]]) = vehicleDetailsResponseSuccess,
                                             bruteForceService: BruteForcePreventionService = bruteForceServiceImpl(permitted = true),
                                             isPrototypeBannerVisible: Boolean = true): (VehicleLookup, VehicleAndKeeperLookupWebService) = {

    val (status, vehicleDetailsResponse) = fullResponse
    val responseAsJson: Option[JsValue] = (status, vehicleDetailsResponse) match {
      case (_, None) => None
      case (_, Some(response)) => response match {
        case Left(failure) => Some(Json.toJson(failure))
        case Right(success) => Some(Json.toJson(success))
      }
    }
    //val responseAsJson = vehicleDetailsResponse.map(Json.toJson(_))
    val wsMock = mock[VehicleAndKeeperLookupWebService]

    when(wsMock.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
      .thenReturn(Future.successful {
        // Any call to a webservice will always return this successful response.
        new FakeResponse(status = status, fakeJson = responseAsJson)
      })

    val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(wsMock, healthStatsMock)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)

    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.surveyUrl).thenReturn("http://fake/survey/url")
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.

    val vehicleLookup = new VehicleLookup()(
      bruteForceService = bruteForceService,
      vehicleAndKeeperLookupService = vehicleAndKeeperLookupServiceImpl,
      surveyUrl = surveyUrl,
      dateService = dateService,
      clientSideSessionFactory,
      config
    )
    (vehicleLookup, wsMock)
  }

  private lazy val vehicleLookupError = {
    val permitted = true // The lookup is permitted as we want to test failure on the vehicle lookup micro-service step.
    val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]

    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
      .thenReturn(Future.failed(new IllegalArgumentException))

    val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(vehicleAndKeeperLookupWebService,
      healthStatsMock
    )
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)

    new VehicleLookup()(
      bruteForceService = bruteForceServiceImpl(permitted = permitted),
      vehicleAndKeeperLookupService = vehicleAndKeeperLookupServiceImpl,
      surveyUrl = surveyUrl,
      dateService = dateService,
      clientSideSessionFactory,
      config
    )
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber)
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
    vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).present(request)
  }

  private def lookupWithMockConfig(config: Config): VehicleLookup =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[VehicleLookup])

  private def mockSurveyConfig(url: String = "http://test/survey/url"): Config = {
    val config = mock[Config]
    val surveyUrl = url
    when(config.surveyUrl).thenReturn(surveyUrl)
    when(config.prototypeSurveyPrepositionInterval).thenReturn(testDuration)
    when(config.googleAnalyticsTrackingId).thenReturn(None)
    when(config.assetsUrl).thenReturn(None)
    config
  }

  private val testDuration = 7.days.toMillis
  private implicit val dateService = new DateServiceImpl
}
