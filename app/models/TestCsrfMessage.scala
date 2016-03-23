package models

final case class TestCsrfMessage(text: String)

object TestCsrfMessage {
  import play.api.libs.json.Json
  implicit val JsonFormat = Json.format[TestCsrfMessage]
}