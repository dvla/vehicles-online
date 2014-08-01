package webserviceclients.brute_force_prevention

import javax.inject.Inject

import models.BruteForcePreventionModel
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import webserviceclients.brute_force_prevention.BruteForcePreventionResponseDto.JsonFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class BruteForcePreventionServiceImpl @Inject()(config: Config,
                                                      ws: BruteForcePreventionWebService,
                                                      dateService: DateService) extends BruteForcePreventionService {
  private val maxAttempts: Int = config.bruteForcePreventionMaxAttemptsHeader.toInt

  override def isVrmLookupPermitted(vrm: String): Future[BruteForcePreventionModel] =
  // TODO US270 this if-statement is a temporary feature toggle until all developers have Redis setup locally.
    if (config.isBruteForcePreventionEnabled) {
      val returnedFuture = scala.concurrent.Promise[BruteForcePreventionModel]()
      ws.callBruteForce(vrm).map { resp =>
        def permitted(): Unit = {
          Json.fromJson[BruteForcePreventionResponseDto](resp.json).asOpt match {
            case Some(model) =>
              val resultModel = BruteForcePreventionModel.fromResponse(
                permitted = true,
                model,
                dateService,
                maxAttempts
              )
              returnedFuture.success(resultModel)
            case _ =>
              Logger.error(s"Brute force prevention service returned invalid Json: ${resp.json}")
              returnedFuture.failure(new Exception("TODO"))
          }
        }
        def notPermitted = BruteForcePreventionModel.fromResponse(permitted = false, BruteForcePreventionResponseDto(attempts = 0), dateService, maxAttempts = maxAttempts)
        resp.status match {
          case play.api.http.Status.OK => permitted()
          case play.api.http.Status.FORBIDDEN => returnedFuture.success(notPermitted)
          case _ => returnedFuture.failure(new Exception("unknownPermission"))
        }
      }.recover {
        case e: Throwable =>
          Logger.error(s"Brute force prevention service throws: ${e.getStackTraceString}")
          returnedFuture.failure(e)
      }
      returnedFuture.future
    }
    else Future {
      BruteForcePreventionModel.fromResponse(permitted = true, BruteForcePreventionResponseDto(attempts = 0), dateService, maxAttempts = maxAttempts)
    }
}
