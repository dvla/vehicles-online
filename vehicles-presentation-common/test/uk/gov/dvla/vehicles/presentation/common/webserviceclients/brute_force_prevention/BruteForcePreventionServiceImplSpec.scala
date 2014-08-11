package uk.gov.dvla.vehicles.presentation.common.webserviceclients.brute_force_prevention

import org.mockito.Mockito.when
import play.api.libs.ws.Response
import uk.gov.dvla.vehicles.presentation.common.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionConfig, BruteForcePreventionWebService, BruteForcePreventionServiceImpl, BruteForcePreventionService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.responseFirstAttempt
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.responseSecondAttempt
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.VrmThrows
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.{FakeDateServiceImpl, FakeResponse}

final class BruteForcePreventionServiceImplSpec extends UnitSpec {
  "isVrmLookupPermitted" should {
    "return true when response status is 200 OK" in {
      val service = bruteForceServiceImpl(permitted = true)
      whenReady(service.isVrmLookupPermitted(RegistrationNumberValid), timeout) {
        case viewModel =>
          viewModel.permitted should equal(true)
          viewModel.attempts should equal(1)
          viewModel.maxAttempts should equal(3)
          viewModel.dateTimeISOChronology should startWith("1970-11-25T00:00:00.000")
      }
    }

    "return false when response status is not 200 OK" in {
      val service = bruteForceServiceImpl(permitted = false)
      whenReady(service.isVrmLookupPermitted(RegistrationNumberValid)) {
        case viewModel =>
          viewModel.permitted should equal(false)
          viewModel.attempts should equal(1)
          viewModel.maxAttempts should equal(3)
          viewModel.dateTimeISOChronology should startWith("1970-11-25T00:00:00.000")
      }
    }

    "fail future when webservice call throws" in {
      val service = bruteForceServiceImpl(permitted = true)
      val result = service.isVrmLookupPermitted(VrmThrows)

      Try(
        whenReady(result){ r => fail("whenReady should throw") }
      ).isFailure should equal(true)
    }
  }

  private def responseThrows: Future[Response] = Future {
    throw new RuntimeException("This error is generated deliberately by a test")
  }

  private def bruteForceServiceImpl(permitted: Boolean): BruteForcePreventionService = {
    def bruteForcePreventionWebService: BruteForcePreventionWebService = {
      val status = if (permitted) play.api.http.Status.OK else play.api.http.Status.FORBIDDEN
      val bruteForcePreventionWebService: BruteForcePreventionWebService = mock[BruteForcePreventionWebService]

      when(bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid)).thenReturn(Future {
        new FakeResponse(status = status, fakeJson = responseFirstAttempt)
      })
      when(bruteForcePreventionWebService.callBruteForce(FakeBruteForcePreventionWebServiceImpl.VrmAttempt2))
        .thenReturn(Future {
          new FakeResponse(status = status, fakeJson = responseSecondAttempt)
        })
      when(bruteForcePreventionWebService.callBruteForce(FakeBruteForcePreventionWebServiceImpl.VrmLocked))
        .thenReturn(Future {
          new FakeResponse(status = status)
        })
      when(bruteForcePreventionWebService.callBruteForce(VrmThrows)).thenReturn(responseThrows)

      bruteForcePreventionWebService
    }

    new BruteForcePreventionServiceImpl(
      new BruteForcePreventionConfig,
      ws = bruteForcePreventionWebService,
      dateService = new FakeDateServiceImpl
    )
  }
}
