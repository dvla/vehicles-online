package models

import play.api.data.Forms._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.{VehicleRegistrationNumber, DocumentReferenceNumber}

final case class VehicleLookupFormModel(referenceNumber: String,
                                        registrationNumber: String)

object VehicleLookupFormModel {
  implicit val JsonFormat = Json.format[VehicleLookupFormModel]
  final val VehicleLookupFormModelCacheKey = s"${CacheKeyPrefix}vehicleLookupFormModel"
  implicit val Key = CacheKey[VehicleLookupFormModel](VehicleLookupFormModelCacheKey)
  final val VehicleLookupResponseCodeCacheKey = s"${CacheKeyPrefix}vehicleLookupResponseCode"

    object Form {
    final val DocumentReferenceNumberId = "documentReferenceNumber"
    final val VehicleRegistrationNumberId = "vehicleRegistrationNumber"

    final val Mapping = mapping(
      DocumentReferenceNumberId -> DocumentReferenceNumber.referenceNumber,
      VehicleRegistrationNumberId -> VehicleRegistrationNumber.registrationNumber
    )(VehicleLookupFormModel.apply)(VehicleLookupFormModel.unapply)
  }
}
