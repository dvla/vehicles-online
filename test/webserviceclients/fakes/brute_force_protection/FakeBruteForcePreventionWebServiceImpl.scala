package webserviceclients.fakes.brute_force_protection

import play.api.http.Status.{FORBIDDEN, OK}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import webserviceclients.fakes.FakeResponse

final class FakeBruteForcePreventionWebServiceImpl() extends BruteForcePreventionWebService {
  import FakeBruteForcePreventionWebServiceImpl.{responseFirstAttempt, VrmLocked}

  override def callBruteForce(vrm: String, trackingId: TrackingId): Future[WSResponse] = Future.successful {
    vrm match {
      case VrmLocked => new FakeResponse(status = FORBIDDEN)
      case _ => new FakeResponse(status = OK, fakeJson = responseFirstAttempt)
    }
  }

  override def reset(vrm: String, trackingId: TrackingId): Future[WSResponse] = Future.successful {
    FakeResponse(status = OK)
  }
}

object FakeBruteForcePreventionWebServiceImpl {
  final val VrmAttempt2 = "ST05YYB"
  final val VrmLocked = "ST05YYC"
  final val VrmThrows = "ST05YYD"
  final val MaxAttempts = 3
  lazy val responseFirstAttempt = Some(Json.parse(s"""{"attempts":0}"""))
  lazy val responseSecondAttempt = Some(Json.parse(s"""{"attempts":1}"""))
}
