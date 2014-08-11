package uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.gds

import uk.gov.dvla.vehicles.presentation.common.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import ClearTextClientSideSessionFactory.DefaultTrackingId
import org.mockito.Mockito.when
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Second, Span}
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.Response
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.gds.AddressLookupServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.gds.domain.Address
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.gds.domain.JsonFormats.addressFormat
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.gds
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeAddressLookupService.PostcodeValid
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeAddressLookupWebServiceImpl.{gdsAddress, traderUprnValid}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.{FakeAddressLookupWebServiceImpl, FakeResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class GdsPostcodeLookupSpec extends UnitSpec {

  /*
    The service will:
    1) Send postcode string to GDS micro-service
    2) Get a response from the GDS micro-service
    3) Translate the response into a Seq that can be used by the drop-down
    */
  "fetchAddressesForPostcode" should {
    "return empty seq when cannot connect to micro-service" in {
      val service = addressServiceMock(responseTimeout)

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result, Timeout(Span(1, Second))) { _ shouldBe empty }
    }

    "return empty seq when response throws" in {
      val service = addressServiceMock(responseThrows)

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result, Timeout(Span(1, Second))) { _ shouldBe empty }
    }

    "return empty seq when micro-service returns invalid JSON" in {
      val inputAsJson = Json.toJson("INVALID")
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result, timeout) { _ shouldBe empty }
    }

    "return empty seq when micro-service response status is not 200 (OK)" in {
      val input: Seq[Address] = Seq(gdsAddress())
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(NOT_FOUND, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result) { _ shouldBe empty }
    }


    "return empty seq when micro-service returns empty seq (meaning no addresses found)" in {
      val expectedResults: Seq[Address] = Seq.empty
      val inputAsJson = Json.toJson(expectedResults)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result) { _ shouldBe empty }
    }

    "return seq of (uprn, address) when micro-service returns a single address" in {
      val expected = (traderUprnValid.toString, s"property stub, 123, town stub, area stub, $PostcodeValid")
      val input: Seq[Address] = Seq(gdsAddress())
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result) { _ shouldBe Seq(expected) }
    }

    "return seq of (uprn, address) when micro-service returns many addresses" in {
      val expected = (traderUprnValid.toString, s"property stub, 123, town stub, area stub, $PostcodeValid")
      val input = Seq(gdsAddress(), gdsAddress(), gdsAddress())
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result) { _ shouldBe Seq(expected, expected, expected) }
    }

    "not throw when an address contains a building number that contains letters" in {
      val expected = Seq(
        (traderUprnValid.toString, s"property stub, 789C, town stub, area stub, $PostcodeValid"),
        (traderUprnValid.toString, s"presentationProperty BBB, 123B, town stub, area stub, $PostcodeValid"),
        (traderUprnValid.toString, s"presentationProperty AAA, 123A, town stub, area stub, $PostcodeValid")
      )
      val input = Seq(
        gdsAddress(presentationStreet = "789C"),
        gdsAddress(presentationProperty = "presentationProperty BBB", presentationStreet = "123B"),
        gdsAddress(presentationProperty = "presentationProperty AAA", presentationStreet = "123A")
      )
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result) { _ shouldBe expected }
    }

    "return seq of (uprn, address) sorted by building number then building name" in {
      val expected = Seq(
        (traderUprnValid.toString, s"property stub, 789, town stub, area stub, $PostcodeValid"),
        (traderUprnValid.toString, s"presentationProperty BBB, 123, town stub, area stub, $PostcodeValid"),
        (traderUprnValid.toString, s"presentationProperty AAA, 123, town stub, area stub, $PostcodeValid")
      )
      val input = Seq(
        gdsAddress(presentationStreet = "789"),
        gdsAddress(presentationProperty = "presentationProperty BBB", presentationStreet = "123"),
        gdsAddress(presentationProperty = "presentationProperty AAA", presentationStreet = "123")
      )
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, DefaultTrackingId)

      whenReady(result) { _ shouldBe expected }
    }
  }

  "fetchAddressForUprn" should {
    "return None when cannot connect to micro-service" in {
      val service = addressServiceMock(responseTimeout)

      val result = service.fetchAddressForUprn(traderUprnValid.toString, DefaultTrackingId)

      whenReady(result) { _ shouldBe None }
    }

    "return None when response throws" in {
      val service = addressServiceMock(responseThrows)

      val result = service.fetchAddressForUprn(traderUprnValid.toString, DefaultTrackingId)

      whenReady(result) { _ shouldBe None }
    }

    "return None when micro-service returns invalid JSON" in {
      val inputAsJson = Json.toJson("INVALID")
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressForUprn(traderUprnValid.toString, DefaultTrackingId)

      whenReady(result) { _ shouldBe None }
    }

    "return None when micro-service response status is not 200 (OK)" in {
      val input: Seq[Address] = Seq(gdsAddress())
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(NOT_FOUND, inputAsJson))

      val result = service.fetchAddressForUprn(traderUprnValid.toString, DefaultTrackingId)

      whenReady(result) { _ shouldBe None }
    }

    "return None when micro-service returns empty seq (meaning no addresses found)" in {
      val inputAsJson = Json.toJson(Seq.empty)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressForUprn(traderUprnValid.toString, DefaultTrackingId)

      whenReady(result) { _ shouldBe None }
    }

    "return AddressViewModel when micro-service returns a single address" in {
      val expected = Seq(
        s"presentationProperty stub, 123, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      val input: Seq[Address] = Seq(gdsAddress())
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressForUprn(traderUprnValid.toString, DefaultTrackingId)

      whenReady(result) {
        case Some(addressViewModel) =>
          addressViewModel.uprn should equal(Some(traderUprnValid.toLong))
          addressViewModel.address === expected
        case _ => fail("Should have returned Some(AddressViewModel)")
      }
    }

    "return AddressViewModel of the first in the seq when micro-service returns many addresses" in {
      val expected = Seq(
        s"presentationProperty stub, 123, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      val input: Seq[Address] = Seq(gdsAddress(), gdsAddress(), gdsAddress())
      val inputAsJson = Json.toJson(input)
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressForUprn(traderUprnValid.toString, DefaultTrackingId)

      whenReady(result) {
        case Some(addressViewModel) =>
          addressViewModel.uprn should equal(Some(traderUprnValid.toLong))
          addressViewModel.address === expected
        case _ => fail("Should have returned Some(AddressViewModel)")
      }
    }
  }
  private def addressServiceMock(response: Future[Response]): AddressLookupService = {
    // Using the real address lookup service but passing in a fake web service that returns the responses we specify.
    new AddressLookupServiceImpl(
      new FakeAddressLookupWebServiceImpl(responseOfPostcodeWebService = response, responseOfUprnWebService = response)
    )
  }

  private def response(statusCode: Int, inputAsJson: JsValue) = Future {
    FakeResponse(status = statusCode, fakeJson = Some(inputAsJson))
  }

  private val responseThrows = Future {
    val response = mock[Response]
    when(response.status).thenThrow(new RuntimeException("This error is generated deliberately by a test"))
    response
  }

  private val responseTimeout = Future {
    val response = mock[Response]
    when(response.status).thenThrow(
      new java.util.concurrent.TimeoutException("This error is generated deliberately by a test")
    )
    response
  }
}
