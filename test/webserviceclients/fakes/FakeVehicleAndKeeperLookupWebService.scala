package webserviceclients.fakes

import org.joda.time.DateTime
import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupDetailsDto
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupErrorMessage
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupResponse
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService

final class FakeVehicleAndKeeperLookupWebService extends VehicleAndKeeperLookupWebService {
  import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseVRMNotFound
  import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseDocRefNumberNotLatest
  import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsKeeperStillOnRecordResponseSuccess
  import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseNotFoundResponseCode
  import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseSuccess

  override def invoke(request: VehicleAndKeeperLookupRequest, trackingId: TrackingId) = Future {
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
    // Any call to a webservice will always return this successful response.
    new FakeResponse(status = responseStatus, fakeJson = Some(responseAsJson))
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
  final val VrmNotFound = VehicleAndKeeperLookupErrorMessage(code = "", message = "vehicle_lookup_vrm_not_found")
  final val DocumentRecordMismatch = VehicleAndKeeperLookupErrorMessage(code = "",
    message = "vehicle_lookup_document_record_mismatch"
  )
  final val UnhandledException = VehicleAndKeeperLookupErrorMessage(code = "", message = "unhandled_exception")
  final val TransactionTimestampValid = new DateTime()

  // TODO : Use proper values here
  private def vehicleDetails(disposeFlag: Boolean = false,
                              suppressedV5CFlag: Boolean = false) =
    VehicleAndKeeperLookupDetailsDto(
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
      keeperChangeDate = None,
      suppressedV5Flag = Some(suppressedV5CFlag)
    )

  val vehicleDetailsResponseSuccess: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, Some(VehicleAndKeeperLookupResponse(responseCode = None, Some(vehicleDetails()))))

  val vehicleDetailsDisposedVehicleResponseSuccess: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, Some(VehicleAndKeeperLookupResponse(responseCode = None, Some(vehicleDetails(disposeFlag = true)))))

  val vehicleDetailsKeeperStillOnRecordResponseSuccess: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, Some(VehicleAndKeeperLookupResponse(responseCode = None, Some(vehicleDetails(disposeFlag = false)))))

  val vehicleDetailsResponseVRMNotFound: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, Some(VehicleAndKeeperLookupResponse(responseCode = Some(VrmNotFound), None)))

  val vehicleDetailsResponseDocRefNumberNotLatest: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, Some(VehicleAndKeeperLookupResponse(responseCode = Some(DocumentRecordMismatch), None)))

  val vehicleDetailsResponseUnhandledException: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, Some(VehicleAndKeeperLookupResponse(responseCode = Some(DocumentRecordMismatch), None)))

  val vehicleDetailsResponseNotFoundResponseCode: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, Some(VehicleAndKeeperLookupResponse(responseCode = None, None)))

  val vehicleDetailsServerDown: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (SERVICE_UNAVAILABLE, None)

  val vehicleDetailsNoResponse: (Int, Option[VehicleAndKeeperLookupResponse]) =
    (OK, None)
}