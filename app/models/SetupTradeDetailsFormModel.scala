package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import common.mappings.Postcode.postcode
import common.mappings.BusinessName.businessNameMapping

// TODO the names of the params repeat names from the model so refactor
final case class SetupTradeDetailsFormModel(traderBusinessName: String, traderPostcode: String)

object SetupTradeDetailsFormModel {
  implicit val JsonFormat = Json.format[SetupTradeDetailsFormModel]
  final val SetupTradeDetailsCacheKey = s"${CacheKeyPrefix}setupTraderDetails"
  implicit val Key = CacheKey[SetupTradeDetailsFormModel](SetupTradeDetailsCacheKey)

  object Form {
    final val TraderNameId = "traderName"
    final val TraderPostcodeId = "traderPostcode"

    final val Mapping = mapping(
      TraderNameId -> businessNameMapping,
      TraderPostcodeId -> postcode
    )(SetupTradeDetailsFormModel.apply)(SetupTradeDetailsFormModel.unapply)
  }
}
