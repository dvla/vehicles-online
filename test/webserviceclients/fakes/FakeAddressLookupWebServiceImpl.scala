package webserviceclients.fakes

import play.api.http.Status.OK
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.webserviceclients.addresslookup.AddressLookupWebService
import common.webserviceclients.addresslookup.ordnanceservey.AddressDto
import webserviceclients.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeWithoutAddresses}

import scala.concurrent.Future

final class FakeAddressLookupWebServiceImpl(responseOfPostcodeWebService: Future[WSResponse])
  extends AddressLookupWebService {

  override def callAddresses(postcode: String,
                             trackingId: TrackingId)
                            (implicit lang: Lang): Future[WSResponse] =
    if (postcode == PostcodeWithoutAddresses.toUpperCase) Future.successful {
      FakeResponse(status = OK, fakeJson = None)
    }
    else responseOfPostcodeWebService

}

object FakeAddressLookupWebServiceImpl {
  final val selectedAddress = "presentationProperty stub, 123, property stub, street stub, town stub, area stub, QQ99QQ"

  private def addressSeq(houseName: String, houseNumber: String): Seq[String] = {
    Seq(houseName, houseNumber, "property stub", "street stub", "town stub", "area stub", PostcodeValid)
  }

def addressesResponseValid: Seq[AddressDto] = {
  val result = Seq(
    AddressDto(addressSeq("presentationProperty stub", "123").mkString(", "),
      None,
      s"123",
      None,
      None,
      s"town stub",
      PostcodeValid
    ),
    AddressDto(addressSeq("presentationProperty stub", "456").mkString(", "),
      None,
      s"123",
      None,
      None,
      s"town stub",
      PostcodeValid),
    AddressDto(addressSeq("presentationProperty stub", "789").mkString(", "),
      None,
      s"123",
      None,
      None,
      s"town stub",
      PostcodeValid)
  )

  result
}

  def responseValidForPostcodeToAddress: Future[WSResponse] = {
    val inputAsJson = Json.toJson(addressesResponseValid)

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseValidForPostcodeToAddressNotFound: Future[WSResponse] = {
    val inputAsJson = Json.toJson(Seq.empty[AddressDto])

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

}
