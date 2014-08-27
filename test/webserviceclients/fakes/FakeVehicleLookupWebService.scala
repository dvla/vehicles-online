package webserviceclients.fakes

import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehiclelookup.{VehicleDetailsDto, VehicleDetailsRequestDto, VehicleDetailsResponseDto, VehicleLookupWebService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class FakeVehicleLookupWebService extends VehicleLookupWebService {
  import webserviceclients.fakes.FakeVehicleLookupWebService._

  override def callVehicleLookupService(request: VehicleDetailsRequestDto, trackingId: String) = Future.successful {
    val (responseStatus, response) = {
      request.referenceNumber match {
        case "99999999991" => vehicleDetailsResponseVRMNotFound
        case "99999999992" => vehicleDetailsResponseDocRefNumberNotLatest
        case "99999999999" => vehicleDetailsResponseNotFoundResponseCode
        case _ => vehicleDetailsResponseSuccess
      }
    }
    val responseAsJson = Json.toJson(response)
    //Logger.debug(s"FakeVehicleLookupWebService callVehicleLookupService with: $responseAsJson")
    new FakeResponse(status = responseStatus, fakeJson = Some(responseAsJson)) // Any call to a webservice will always return this successful response.
  }
}

object FakeVehicleLookupWebService {
  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val VehicleMakeValid = "Alfa Romeo"
  final val VehicleModelValid = "Alfasud ti"
  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"

  private val vehicleDetails = VehicleDetailsDto(registrationNumber = RegistrationNumberValid,
    vehicleMake = VehicleMakeValid,
    vehicleModel = VehicleModelValid)

  val vehicleDetailsResponseSuccess: (Int, Option[VehicleDetailsResponseDto]) = {
    (OK, Some(VehicleDetailsResponseDto(responseCode = None, vehicleDetailsDto = Some(vehicleDetails))))
  }

  val vehicleDetailsResponseVRMNotFound: (Int, Option[VehicleDetailsResponseDto]) = {
    (OK, Some(VehicleDetailsResponseDto(responseCode = Some("vehicle_lookup_vrm_not_found"), vehicleDetailsDto = None))) // TODO make response code a constant
  }

  val vehicleDetailsResponseDocRefNumberNotLatest: (Int, Option[VehicleDetailsResponseDto]) = {
    (OK, Some(VehicleDetailsResponseDto(
      responseCode = Some("vehicle_lookup_document_record_mismatch"),
      vehicleDetailsDto = None
    )))
  }

  val vehicleDetailsResponseNotFoundResponseCode: (Int, Option[VehicleDetailsResponseDto]) = {
    (OK, Some(VehicleDetailsResponseDto(responseCode = None, vehicleDetailsDto = None)))
  }

  val vehicleDetailsServerDown: (Int, Option[VehicleDetailsResponseDto]) = {
    (SERVICE_UNAVAILABLE, None)
  }

  val vehicleDetailsNoResponse: (Int, Option[VehicleDetailsResponseDto]) = {
    (OK, None)
  }
}
