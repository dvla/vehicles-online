package models.domain.disposal_of_vehicle

import play.api.libs.json.Json

case class UprnAddressPair(uprn: String, address: String)

object UprnAddressPair {
  implicit val JsonFormat = Json.format[UprnAddressPair]
}
