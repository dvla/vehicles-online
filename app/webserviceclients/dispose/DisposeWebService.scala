package webserviceclients.dispose

import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait DisposeWebService {
  def callDisposeService(request: DisposeRequestDto, trackingId: TrackingId): Future[WSResponse]
}
