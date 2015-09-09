package controllers

import composition.WithApplication
import helpers.UnitSpec
import helpers.disposal_of_vehicle.InvalidVRMFormat.allInvalidVrmFormats
import helpers.disposal_of_vehicle.ValidVRMFormat.allValidVrmFormats
import models.VehicleLookupFormModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import common.services.DateServiceImpl
import common.testhelpers.RandomVrmGenerator
import common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionServiceImpl
import common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import common.webserviceclients.healthstats.HealthStats
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupResponse
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupServiceImpl
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import utils.helpers.Config
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.ConsentValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.ReferenceNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.vehicleDetailsResponseSuccess
import webserviceclients.fakes.{FakeDateServiceImpl, FakeResponse}

class VehicleLookupFormSpec extends UnitSpec {
  implicit val dateService = new DateServiceImpl

  "form" should {
    "accept when all fields contain valid responses" in new WithApplication {
      formWithValidDefaults().get.referenceNumber should equal(ReferenceNumberValid)
      formWithValidDefaults().get.registrationNumber should equal(RegistrationNumberValid)
    }
  }

  "referenceNumber" should {
    allInvalidVrmFormats.foreach(vrm => "reject invalid vehicle registration mark : " + vrm in new WithApplication {
      formWithValidDefaults(registrationNumber = vrm).errors should have length 1
    })

    allValidVrmFormats.foreach(vrm => "accept valid vehicle registration mark : " + vrm in new WithApplication {
      formWithValidDefaults(registrationNumber = vrm).get.registrationNumber should equal(vrm)
    })

    "reject if blank" in new WithApplication {
      val vehicleLookupFormError = formWithValidDefaults(referenceNumber = "").errors
      val expectedKey = DocumentReferenceNumberId

      vehicleLookupFormError should have length 3
      vehicleLookupFormError.head.key should equal(expectedKey)
      vehicleLookupFormError.head.message should equal("error.minLength")
      vehicleLookupFormError(1).key should equal(expectedKey)
      vehicleLookupFormError(1).message should equal("error.required")
      vehicleLookupFormError(2).key should equal(expectedKey)
      vehicleLookupFormError(2).message should equal("error.restricted.validNumberOnly")
    }

    "reject if less than min length" in new WithApplication {
      formWithValidDefaults(referenceNumber = "1234567891").errors should have length 1
    }

    "reject if greater than max length" in new WithApplication {
      formWithValidDefaults(referenceNumber = "123456789101").errors should have length 1
    }

    "reject if contains letters" in new WithApplication {
      formWithValidDefaults(referenceNumber = "qwertyuiopl").errors should have length 1
    }

    "reject if contains special characters" in new WithApplication {
      formWithValidDefaults(referenceNumber = "£££££££££££").errors should have length 1
    }

    "accept if valid" in new WithApplication {
      formWithValidDefaults(registrationNumber = RegistrationNumberValid).get.referenceNumber should
        equal(ReferenceNumberValid)
    }
  }

  "registrationNumber" should {
    "reject if empty" in new WithApplication {
      formWithValidDefaults(registrationNumber = "").errors should have length 3
    }

    "reject if less than min length" in new WithApplication {
      formWithValidDefaults(registrationNumber = "a").errors should have length 2
    }

    "reject if more than max length" in new WithApplication {
      formWithValidDefaults(registrationNumber = "AB53WERT").errors should have length 1
    }

    "reject if more than max length 2" in new WithApplication {
      formWithValidDefaults(registrationNumber = "PJ056YYY").errors should have length 1
    }

    "reject if contains special characters" in new WithApplication {
      formWithValidDefaults(registrationNumber = "ab53ab%").errors should have length 1
    }

    "accept a selection of randomly generated vrms that all satisfy vrm regex" in new WithApplication {
      for (i <- 1 to 100) {
        val randomVrm = RandomVrmGenerator.uniqueVrm
        formWithValidDefaults(registrationNumber = randomVrm).get.registrationNumber should equal(randomVrm)
      }
    }
  }

  private lazy val bruteForceServiceImpl: BruteForcePreventionService = {
    val bruteForcePreventionWebService: BruteForcePreventionWebService = mock[BruteForcePreventionWebService]
    when(bruteForcePreventionWebService.callBruteForce(anyString(), any[TrackingId])).
      thenReturn( Future.successful( new FakeResponse(status = OK) ))
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] =
        invocation.getArguments()(1).asInstanceOf[Future[_]]
    })

    new BruteForcePreventionServiceImpl(
      config = new BruteForcePreventionConfig,
      ws = bruteForcePreventionWebService,
      healthStatsMock,
      dateService = new FakeDateServiceImpl
    )
  }

  private def vehicleLookupResponseGenerator(fullResponse:(Int, Option[VehicleAndKeeperLookupResponse])) = {
    val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]

    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId])).
      thenReturn(Future.successful {
        val responseAsJson : Option[JsValue] = fullResponse._2 match {
          case Some(e) => Some(Json.toJson(e))
          case _ => None
        }
        // Any call to a webservice will always return this successful response.
        new FakeResponse(status = fullResponse._1, fakeJson = responseAsJson)
      })
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] =
        invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val vehicleAndKeeperLookupServiceImpl =
      new VehicleAndKeeperLookupServiceImpl(vehicleAndKeeperLookupWebService, healthStatsMock)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
    new VehicleLookup()(
      bruteForceService = bruteForceServiceImpl,
      vehicleAndKeeperLookupService = vehicleAndKeeperLookupServiceImpl,
      surveyUrl,
      dateService,
      clientSideSessionFactory,
      config
    )
  }

  private def formWithValidDefaults(referenceNumber: String = ReferenceNumberValid,
                                    registrationNumber: String = RegistrationNumberValid,
                                    consent: String = ConsentValid) = {
    vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).form.bind(
      Map(
        DocumentReferenceNumberId -> referenceNumber,
        VehicleRegistrationNumberId -> registrationNumber
      )
    )
  }
}
