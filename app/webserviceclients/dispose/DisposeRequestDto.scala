package webserviceclients.dispose

import play.api.libs.json.Json

final case class DisposeRequestDto(referenceNumber: String,
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
