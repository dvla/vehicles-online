package webserviceclients.dispose

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto

final case class DisposeRequestDto(webHeader: VssWebHeaderDto,
                                   referenceNumber: String,
                                   registrationNumber: String,
                                   traderName: String,
                                   traderAddress: DisposalAddressDto,
                                   dateOfDisposal: String,
                                   transactionTimestamp: String,
                                   prConsent: Boolean,
                                   keeperConsent: Boolean,
                                   mileage: Option[Int] = None)

object DisposeRequestDto {
  implicit val JsonFormat = Json.writes[DisposeRequestDto]
}
