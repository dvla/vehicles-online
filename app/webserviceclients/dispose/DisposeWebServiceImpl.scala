package webserviceclients.dispose

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders

final class DisposeWebServiceImpl @Inject()(config: DisposeConfig)  extends DisposeWebService {
  private val endPoint: String = s"${config.baseUrl}/vehicles/dispose/v1"
  private val requestTimeout: Int = config.requestTimeout

  override def callDisposeService(request: DisposeRequestDto, trackingId: TrackingId): Future[WSResponse] =
    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId.value)
      .withRequestTimeout(requestTimeout)
      .post(Json.toJson(request))
}
