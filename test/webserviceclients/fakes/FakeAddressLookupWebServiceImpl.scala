package webserviceclients.fakes

import play.api.http.Status.OK
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.webserviceclients.addresslookup.AddressLookupWebService
import common.webserviceclients.addresslookup.gds.domain.Address
import common.webserviceclients.addresslookup.gds.domain.Details
import common.webserviceclients.addresslookup.gds.domain.Location
import common.webserviceclients.addresslookup.gds.domain.Presentation
import common.webserviceclients.addresslookup.ordnanceservey.PostcodeToAddressResponseDto
import common.webserviceclients.addresslookup.ordnanceservey.AddressResponseDto
import scala.concurrent.Future
import webserviceclients.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeWithoutAddresses}

final class FakeAddressLookupWebServiceImpl(responseOfPostcodeWebService: Future[WSResponse])
  extends AddressLookupWebService {

  override def callPostcodeWebService(postcode: String,
                                      trackingId: TrackingId)
                                     (implicit lang: Lang): Future[WSResponse] =
    if (postcode == PostcodeWithoutAddresses.toUpperCase) Future.successful {
      FakeResponse(status = OK, fakeJson = None)
    }
    else responseOfPostcodeWebService

  override def callAddresses(postcode: String, trackingId: TrackingId)
                            (implicit lang: Lang): Future[WSResponse] = ???
}

object FakeAddressLookupWebServiceImpl {
  final val selectedAddress = "presentationProperty stub, 123, property stub, street stub, town stub, area stub, QQ99QQ"

  private def addressSeq(houseName: String, houseNumber: String): Seq[String] = {
    Seq(houseName, houseNumber, "property stub", "street stub", "town stub", "area stub", PostcodeValid)
  }

  def postcodeToAddressResponseValid: PostcodeToAddressResponseDto = {
    val results = Seq(
      AddressResponseDto(addressSeq("presentationProperty stub", "123").mkString(", "), None),
      AddressResponseDto(addressSeq("presentationProperty stub", "456").mkString(", "), None),
      AddressResponseDto(addressSeq("presentationProperty stub", "789").mkString(", "), None)
    )

    PostcodeToAddressResponseDto(addresses = results)
  }

  def responseValidForPostcodeToAddress: Future[WSResponse] = {
    val inputAsJson = Json.toJson(postcodeToAddressResponseValid)

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseValidForPostcodeToAddressNotFound: Future[WSResponse] = {
    val inputAsJson = Json.toJson(PostcodeToAddressResponseDto(addresses = Seq.empty))

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

}
