package helpers

import org.scalatest.Assertions.fail
import play.api.libs.json.{Json, Reads}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

object JsonUtils {

  def deserializeJsonToModel[A](json: String)(implicit fjs: Reads[A], cacheKey: CacheKey[A]): A = {
    val parsedJsValue = Json.parse(json)
    val fromJson = Json.fromJson[A](parsedJsValue)
    fromJson.asEither match {
      case Left(errors) =>
        fail(s"Failed to deserialize this json: $json into a model of type ${cacheKey.value}")
      case Right(model) =>
        model
    }
  }
}