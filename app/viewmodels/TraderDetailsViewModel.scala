package viewmodels

import models.AddressModel
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class TraderDetailsViewModel(traderName: String, traderAddress: AddressModel)

object TraderDetailsViewModel {
  implicit val JsonFormat = Json.format[TraderDetailsViewModel]
  final val TraderDetailsCacheKey = "traderDetails"
  implicit val Key = CacheKey[TraderDetailsViewModel](value = TraderDetailsCacheKey)
}
