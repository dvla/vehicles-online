package models

import play.api.data.Forms.mapping
import play.api.data.Mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import common.mappings.Postcode.postcode
import common.views.constraints.BusinessName
import common.views.helpers.FormExtensions.nonEmptyTextWithTransform

// TODO the names of the params repeat names from the model so refactor
final case class SetupTradeDetailsFormModel(traderBusinessName: String, traderPostcode: String)

object SetupTradeDetailsFormModel {
  implicit val JsonFormat = Json.format[SetupTradeDetailsFormModel]
  final val SetupTradeDetailsCacheKey = "setupTraderDetails"
  implicit val Key = CacheKey[SetupTradeDetailsFormModel](SetupTradeDetailsCacheKey)

  object Form {
    final val TraderNameId = "traderName"
    final val TraderPostcodeId = "traderPostcode"
    final val TraderNameMaxLength = 58
    final val TraderNameMinLength = 2

    private final val TraderNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.toUpperCase.trim)(TraderNameMinLength, TraderNameMaxLength)
        .verifying(BusinessName.validBusinessName)

    final val Mapping = mapping(
      TraderNameId -> TraderNameMapping,
      TraderPostcodeId -> postcode
    )(SetupTradeDetailsFormModel.apply)(SetupTradeDetailsFormModel.unapply)
  }
}
