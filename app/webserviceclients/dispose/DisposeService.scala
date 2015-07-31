package webserviceclients.dispose

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait DisposeService {
  def invoke(cmd: DisposeRequestDto, trackingId: TrackingId): Future[(Int, Option[DisposeResponseDto])]
}
