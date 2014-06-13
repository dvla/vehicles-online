package services.fakes.brute_force_protection

import FakeBruteForcePreventionWebServiceImpl._
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.Response
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.brute_force_prevention.BruteForcePreventionWebService
import services.fakes.FakeResponse

final class FakeBruteForcePreventionWebServiceImpl() extends BruteForcePreventionWebService {
  override def callBruteForce(vrm: String): Future[Response] = Future {
    vrm match {
      case VrmLocked => new FakeResponse(status = FORBIDDEN)
      case _ => new FakeResponse(status = OK, fakeJson = responseFirstAttempt)
    }
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