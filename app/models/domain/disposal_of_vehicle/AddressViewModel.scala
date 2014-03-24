package models.domain.disposal_of_vehicle

import play.api.libs.json.Json
import models.domain.common.AddressAndPostcodeModel

case class AddressViewModel(uprn: Option[Long] = None, // Optional because if user is manually entering the address they will not be allowed to enter a UPRN, it is only populated by address lookup services.
                            address: Seq[String])

object AddressViewModel{
  implicit val addressViewModelJson = Json.format[AddressViewModel]

  def from(address: AddressAndPostcodeModel): AddressViewModel = {
    AddressViewModel(address = address.toViewFormat)
  }
}