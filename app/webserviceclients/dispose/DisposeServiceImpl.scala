package webserviceclients.dispose

import javax.inject.Inject
import play.api.Logger
import play.api.http.Status.OK
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.LogFormats

final class DisposeServiceImpl @Inject()(config: DisposeConfig, ws: DisposeWebService) extends DisposeService {

  override def invoke(cmd: DisposeRequestDto, trackingId: String): Future[(Int, Option[DisposeResponseDto])] = {
    val vrm = LogFormats.anonymize(cmd.registrationNumber)
    val refNo = LogFormats.anonymize(cmd.referenceNumber)
    val postcode = LogFormats.anonymize(cmd.traderAddress.postCode)

    Logger.debug("Calling dispose vehicle micro-service with " +
      s"$refNo $vrm $postcode ${cmd.keeperConsent} ${cmd.prConsent} ${cmd.mileage}")

    ws.callDisposeService(cmd, trackingId).map { resp =>
      Logger.debug(s"Http response code from dispose vehicle micro-service was: ${resp.status}")

      if (resp.status == OK) (resp.status, resp.json.asOpt[DisposeResponseDto])
      else (resp.status, None)
    }
  }
}
