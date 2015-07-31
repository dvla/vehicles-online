package webserviceclients.dispose

import javax.inject.Inject
import play.api.Logger
import play.api.http.Status.OK
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.{HealthStatsFailure, HealthStatsSuccess, HealthStats}

final class DisposeServiceImpl @Inject()(config: DisposeConfig,
                                         ws: DisposeWebService,
                                         healthStats: HealthStats,
                                         dateService: DateService) extends DisposeService {
  import DisposeServiceImpl.ServiceName

  override def invoke(cmd: DisposeRequestDto, trackingId: TrackingId): Future[(Int, Option[DisposeResponseDto])] = {

    val vrm = LogFormats.anonymize(cmd.registrationNumber)
    val refNo = LogFormats.anonymize(cmd.referenceNumber)
    val postcode = LogFormats.anonymize(cmd.traderAddress.postCode)

    Logger.debug("Calling dispose vehicle micro-service with " +
      s"$refNo $vrm $postcode ${cmd.keeperConsent} ${cmd.prConsent} ${cmd.mileage}")

    ws.callDisposeService(cmd, trackingId).map { resp =>
      Logger.debug(s"Http response code from dispose vehicle micro-service was: ${resp.status}")

      if (resp.status == OK) {
        healthStats.success(new HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.asOpt[DisposeResponseDto])
      } else {
        healthStats.failure(
          new HealthStatsFailure(ServiceName, dateService.now, new Exception(s"Response code is ${resp.status}"))
        )
        (resp.status, None)
      }
    }
  }.recover{
    case t: Throwable =>
      healthStats.failure(new HealthStatsFailure(ServiceName, dateService.now, t))
      throw t
  }
}

object DisposeServiceImpl {
  final val ServiceName = "vehicle-dispose-fulfil-microservice"
}
