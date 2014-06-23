package models.domain.disposal_of_vehicle

import models.domain.common.CacheKey
import mappings.disposal_of_vehicle.SetupTradeDetails.SetupTradeDetailsCacheKey
import play.api.libs.json.Json


// TODO the names of the params repeat names from the model so refactor
final case class SetupTradeDetailsModel(traderBusinessName: String, traderPostcode: String)

object SetupTradeDetailsModel {
  implicit val JsonFormat = Json.format[SetupTradeDetailsModel]
  implicit val Key = CacheKey[SetupTradeDetailsModel](SetupTradeDetailsCacheKey)

  def convertToUpperCase(model: SetupTradeDetailsModel): SetupTradeDetailsModel =
    model.copy(traderBusinessName = model.traderBusinessName.toUpperCase, traderPostcode = model.traderPostcode.toUpperCase)
}