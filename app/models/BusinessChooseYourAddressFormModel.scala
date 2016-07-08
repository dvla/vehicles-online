package models

import uk.gov.dvla.vehicles.presentation.common.mappings.DropDown
import models.DisposeCacheKeyPrefix.CookiePrefix
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class BusinessChooseYourAddressFormModel(addressSelected: String)

object BusinessChooseYourAddressFormModel {
  implicit val JsonFormat = Json.format[BusinessChooseYourAddressFormModel]
  final val BusinessChooseYourAddressCacheKey = s"${CookiePrefix}businessChooseYourAddress"
  implicit val Key = CacheKey[BusinessChooseYourAddressFormModel](value = BusinessChooseYourAddressCacheKey)

  object Form {
    final val AddressSelectId = "disposal_businessChooseYourAddress_addressSelect"
    final val Mapping = mapping(
      AddressSelectId -> DropDown.addressDropDown
    )(BusinessChooseYourAddressFormModel.apply)(BusinessChooseYourAddressFormModel.unapply)
  }
}
