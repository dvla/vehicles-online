package webserviceclients.dispose

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse

final case class DisposeResponse(transactionId: String,
                                 registrationNumber: String,
                                 auditId: String)

final case class DisposeResponseDto(response: Option[MicroserviceResponse], disposeResponse: DisposeResponse)

object DisposeResponse {
  implicit val JsonFormat = Json.format[DisposeResponse]
}

object DisposeResponseDto {
  implicit val JsonFormat = Json.format[DisposeResponseDto]
}