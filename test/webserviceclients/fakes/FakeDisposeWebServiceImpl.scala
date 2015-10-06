package webserviceclients.fakes

import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import webserviceclients.dispose.{DisposeRequestDto, DisposeResponse, DisposeResponseDto}
import webserviceclients.dispose.DisposeWebService
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberValid

final class FakeDisposeWebServiceImpl extends DisposeWebService {
  import FakeDisposeWebServiceImpl.disposeResponseSoapEndpointFailure
  import FakeDisposeWebServiceImpl.disposeResponseSuccess
  import FakeDisposeWebServiceImpl.SimulateMicroServiceUnavailable
  import FakeDisposeWebServiceImpl.SimulateSoapEndpointFailure

  override def callDisposeService(request: DisposeRequestDto,
                                  trackingId: TrackingId
                                   ): Future[WSResponse] = Future.successful {
    val disposeResponse: DisposeResponseDto = {
      request.referenceNumber match {
        case SimulateMicroServiceUnavailable => throw new RuntimeException("simulateMicroServiceUnavailable")
        case SimulateSoapEndpointFailure => disposeResponseSoapEndpointFailure
        case _ => disposeResponseSuccess
      }
    }
    val responseAsJson = Json.toJson(disposeResponse)
    Logger.debug(s"FakeVehicleLookupWebService callVehicleLookupService with: $responseAsJson")
    // Any call to a webservice will always return this successful response.
    new FakeResponse(status = OK, fakeJson = Some(responseAsJson))
  }
}

object FakeDisposeWebServiceImpl {
  final val TransactionIdValid = TrackingId("1234")
  private final val AuditIdValid = "7575"
  private final val SimulateMicroServiceUnavailable = "8" * 11
  private final val SimulateSoapEndpointFailure = "9" * 11

  val disposeResponseSuccess =
    DisposeResponseDto(
      None,
      DisposeResponse(
        transactionId = TransactionIdValid.value,
        registrationNumber = RegistrationNumberValid,
        auditId = AuditIdValid
      )
    )

  val disposeResponseSoapEndpointFailure =
    DisposeResponseDto(
      None,
      DisposeResponse(transactionId = "", // No transactionId because the soap endpoint is down
        registrationNumber = "",
        auditId = ""
      )
    )

  val disposeResponseFailureWithResponseCode =
    // We should always get back a transaction id even for failure scenarios.
    // Only exception is if the soap endpoint is down
    DisposeResponseDto(
      Some(MicroserviceResponse("US9802", "ms.vehiclesService.response.unableToProcessApplication")),
      DisposeResponse(
        transactionId = TransactionIdValid.value,
        registrationNumber = "",
        auditId = ""
      )
    )

  val disposeResponseFailureWithDuplicateDisposal =
    // We should always get back a transaction id even for failure scenarios.
    // Only exception is if the soap endpoint is down
    DisposeResponseDto(
      Some(MicroserviceResponse("US9302", "ms.vehiclesService.response.duplicateDisposalToTrade")),
      DisposeResponse(
        transactionId = TransactionIdValid.value,
        registrationNumber = "",
        auditId = ""
      )
    )

  val disposeResponseApplicationBeingProcessed =
    DisposeResponseDto(
      Some(MicroserviceResponse("", "ms.vehiclesService.response.applicationBeingProcessed")),
      DisposeResponse(
      transactionId = TransactionIdValid.value,
      registrationNumber = RegistrationNumberValid,
      auditId = AuditIdValid
      )
    )

  val disposeResponseUnableToProcessApplication =
    DisposeResponseDto(
      Some(MicroserviceResponse("U0010", "ms.vehiclesService.response.unableToProcessApplication")),
      DisposeResponse(
        transactionId = "", // No transactionId because the soap endpoint is down
        registrationNumber = "",
        auditId = ""
      )
    )

  val disposeResponseUndefinedError =
    DisposeResponseDto(
      Some(MicroserviceResponse("undefined", "undefined")),
      DisposeResponse(
        transactionId = "", // No transactionId because the soap endpoint is down
        registrationNumber = "",
        auditId = ""
      )
    )

  final val ConsentValid = "true"
  final val MileageValid = "20000"
  final val MileageInvalid = "INVALID"
}
