
package webserviceclients.fakes

import org.joda.time.DateTime
import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class FakeVehicleAndKeeperLookupWebService extends VehicleAndKeeperLookupWebService {
  import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService._

  override def invoke(request: VehicleAndKeeperDetailsRequest, trackingId: String) = Future {
    val (responseStatus, response) = {
      request.referenceNumber match {
        case "99999999991" => vehicleDetailsResponseVRMNotFound
        case "99999999992" => vehicleDetailsResponseDocRefNumberNotLatest
        case "99999999993" => vehicleDetailsKeeperStillOnRecordResponseSuccess
        case "99999999999" => vehicleDetailsResponseNotFoundResponseCode
        case _ => vehicleDetailsResponseSuccess
      }
    }
    val responseAsJson = Json.toJson(response)
    new FakeResponse(status = responseStatus, fakeJson = Some(responseAsJson)) // Any call to a webservice will always return this successful response.
  }
}

object FakeVehicleAndKeeperLookupWebService {
  final val SoldToIndividual = ""
  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val VehicleMakeValid = "Alfa Romeo"
  final val VehicleModelValid = "Alfasud ti"
  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"
  final val TransactionIdValid = "A1-100"
  final val VrmNotFound = "vehicle_lookup_vrm_not_found"
  final val DocumentRecordMismatch = "vehicle_lookup_document_record_mismatch"
  final val TransactionTimestampValid = new DateTime()

  // TODO : Use proper values here
  private def vehicleDetails(disposeFlag: Boolean = false,
                              suppressedV5CFlag: Boolean = false) =
    VehicleAndKeeperDetailsDto(
      registrationNumber = RegistrationNumberValid,
      vehicleMake = Some(VehicleMakeValid),
      vehicleModel = Some(VehicleModelValid),
      keeperTitle = Some("a"),
      keeperFirstName = Some("a"),

      keeperLastName = Some("a"),
      keeperAddressLine1 = Some("a"),
      keeperAddressLine2 = Some("a"),
      keeperAddressLine3 = Some("a"),
      keeperAddressLine4 = Some("a"),
      keeperPostTown = Some("a"),
      keeperPostcode = Some("a"),
      disposeFlag = Some(disposeFlag),
      keeperEndDate = if (disposeFlag) Some(new DateTime()) else None,
      suppressedV5Flag = Some(suppressedV5CFlag)
    )

  val vehicleDetailsResponseSuccess: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = None, Some(vehicleDetails()))))
  }

  val vehicleDetailsKeeperStillOnRecordResponseSuccess: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = None, Some(vehicleDetails(disposeFlag = false)))))
  }

  val vehicleDetailsResponseVRMNotFound: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = Some(VrmNotFound), None)))
  }

  val vehicleDetailsResponseDocRefNumberNotLatest: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = Some(DocumentRecordMismatch), None)))
  }

  val vehicleDetailsResponseNotFoundResponseCode: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = None, None)))
  }

  val vehicleDetailsServerDown: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (SERVICE_UNAVAILABLE, None)
  }

  val vehicleDetailsNoResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, None)
  }
}