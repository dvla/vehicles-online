package webserviceclients.fakes

import play.api.http.Status.OK
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.model.AddressModel
import common.webserviceclients.addresslookup.AddressLookupWebService
import common.webserviceclients.addresslookup.gds.domain.Address
import common.webserviceclients.addresslookup.gds.domain.Details
import common.webserviceclients.addresslookup.gds.domain.Location
import common.webserviceclients.addresslookup.gds.domain.Presentation
import common.webserviceclients.addresslookup.ordnanceservey.PostcodeToAddressResponseDto
import common.webserviceclients.addresslookup.ordnanceservey.AddressResponseDto
import common.webserviceclients.addresslookup.ordnanceservey.UprnToAddressResponseDto
import scala.concurrent.Future
import webserviceclients.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeWithoutAddresses}

final class FakeAddressLookupWebServiceImpl(responseOfPostcodeWebService: Future[WSResponse],
                                            responseOfUprnWebService: Future[WSResponse])
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
  final val traderUprnValid = 999999999999L
  final val traderUprnValid2 = 4567L
  final val selectedAddressLine = "presentationProperty stub, 123, property stub, street stub, town stub, area stub, QQ99QQ"

  private def addressSeq(houseName: String, houseNumber: String): Seq[String] = {
    Seq(houseName, houseNumber, "property stub", "street stub", "town stub", "area stub", PostcodeValid)
  }

  def uprnAddressPairWithDefaults(uprn: String = traderUprnValid.toString,
                                  houseName: String = "presentationProperty stub",
                                  houseNumber: String = "123"
                                   ) =
    AddressResponseDto(address = addressSeq(houseName, houseNumber).mkString(", "), Some(uprn), None)

  def postcodeToAddressResponseValid: PostcodeToAddressResponseDto = {
    val results = Seq(
      uprnAddressPairWithDefaults(),
      uprnAddressPairWithDefaults(uprn = "67890", houseNumber = "456"),
      uprnAddressPairWithDefaults(uprn = "111213", houseNumber = "789")
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

  val uprnToAddressResponseValid = {
    val uprnAddressPair = uprnAddressPairWithDefaults()

    val uprnOpt = uprnAddressPair.uprn match{
      case Some(u) => Some(u.toLong)
      case _ => None
    }

    UprnToAddressResponseDto(addressViewModel = Some(
      AddressModel(uprn = uprnOpt, address = uprnAddressPair.address.split(", ")))
    )
  }

  def responseValidForUprnToAddress: Future[WSResponse] = {
    val inputAsJson = Json.toJson(uprnToAddressResponseValid)

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseValidForUprnToAddressNotFound: Future[WSResponse] = {
    val inputAsJson = Json.toJson(UprnToAddressResponseDto(addressViewModel = None))

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def gdsAddress(presentationProperty: String = "property stub", presentationStreet: String = "123"): Address =
    Address(
      gssCode = "gssCode stub",
      countryCode = "countryCode stub",
      postcode = PostcodeValid,
      houseName = Some("presentationProperty stub"),
      houseNumber = Some("123"),
      presentation = Presentation(property = Some(presentationProperty),
        street = Some(presentationStreet),
        town = Some("town stub"),
        area = Some("area stub"),
        postcode = PostcodeValid,
        uprn = traderUprnValid.toString),
      details = Details(
        usrn = "usrn stub",
        isResidential = true,
        isCommercial = true,
        isPostalAddress = true,
        classification = "classification stub",
        state = "state stub",
        organisation = Some("organisation stub")
      ),
      location = Location(
        x = 1.0d,
        y = 2.0d)
    )

  def responseValidForGdsAddressLookup: Future[WSResponse] = {
    import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.gds.domain.JsonFormats.addressFormat
    val inputAsJson = Json.toJson(Seq(gdsAddress(), gdsAddress(presentationStreet = "456")))

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }
}
