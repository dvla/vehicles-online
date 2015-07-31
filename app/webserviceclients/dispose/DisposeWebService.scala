package webserviceclients.dispose

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Future

// TODO Do we still need this abstraction, now the code base is more mockable?
trait DisposeWebService {
  def callDisposeService(request: DisposeRequestDto, trackingId: TrackingId): Future[WSResponse]
}
