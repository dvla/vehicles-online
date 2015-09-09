package webserviceclients.dispose

import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait DisposeService {
  def invoke(cmd: DisposeRequestDto, trackingId: TrackingId): Future[(Int, Option[DisposeResponseDto])]
}
