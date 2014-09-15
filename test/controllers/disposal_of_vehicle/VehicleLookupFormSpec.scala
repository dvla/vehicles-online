package controllers.disposal_of_vehicle

import controllers.{SurveyUrl, VehicleLookup}
import helpers.UnitSpec
import helpers.common.RandomVrmGenerator
import helpers.disposal_of_vehicle.InvalidVRMFormat.allInvalidVrmFormats
import helpers.disposal_of_vehicle.ValidVRMFormat.allValidVrmFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionConfig, BruteForcePreventionWebService, BruteForcePreventionServiceImpl, BruteForcePreventionService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehiclelookup.{VehicleLookupWebService, VehicleLookupServiceImpl, VehicleDetailsResponseDto, VehicleDetailsRequestDto}
import viewmodels.VehicleLookupFormModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import services.DateServiceImpl
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import webserviceclients.fakes.FakeVehicleLookupWebService.ConsentValid
import webserviceclients.fakes.FakeVehicleLookupWebService.ReferenceNumberValid
import webserviceclients.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleLookupWebService.vehicleDetailsResponseSuccess
import webserviceclients.fakes.{FakeDateServiceImpl, FakeResponse}
import utils.helpers.Config

final class VehicleLookupFormSpec extends UnitSpec {
  implicit val dateService = new DateServiceImpl

  "form" should {
    "accept when all fields contain valid responses" in {
      formWithValidDefaults().get.referenceNumber should equal(ReferenceNumberValid)
      formWithValidDefaults().get.registrationNumber should equal(RegistrationNumberValid)
    }
  }

  "referenceNumber" should {
    allInvalidVrmFormats.map(vrm => "reject invalid vehicle registration mark : " + vrm in {
      formWithValidDefaults(registrationNumber = vrm).errors should have length 1
    })

    allValidVrmFormats.map(vrm => "accept valid vehicle registration mark : " + vrm in {
      formWithValidDefaults(registrationNumber = vrm).get.registrationNumber should equal(vrm)
    })

    "reject if blank" in {
      val vehicleLookupFormError = formWithValidDefaults(referenceNumber = "").errors
      val expectedKey = DocumentReferenceNumberId
      
      vehicleLookupFormError should have length 3
      vehicleLookupFormError(0).key should equal(expectedKey)
      vehicleLookupFormError(0).message should equal("error.minLength")
      vehicleLookupFormError(1).key should equal(expectedKey)
      vehicleLookupFormError(1).message should equal("error.required")
      vehicleLookupFormError(2).key should equal(expectedKey)
      vehicleLookupFormError(2).message should equal("error.restricted.validNumberOnly")
    }

    "reject if less than min length" in {
      formWithValidDefaults(referenceNumber = "1234567891").errors should have length 1
    }

    "reject if greater than max length" in {
      formWithValidDefaults(referenceNumber = "123456789101").errors should have length 1
    }

    "reject if contains letters" in {
      formWithValidDefaults(referenceNumber = "qwertyuiopl").errors should have length 1
    }

    "reject if contains special characters" in {
      formWithValidDefaults(referenceNumber = "£££££££££££").errors should have length 1
    }

    "accept if valid" in {
      formWithValidDefaults(registrationNumber = RegistrationNumberValid).get.referenceNumber should equal(ReferenceNumberValid)
    }
  }

  "registrationNumber" should {
    "reject if empty" in {
      formWithValidDefaults(registrationNumber = "").errors should have length 3
    }

    "reject if less than min length" in {
      formWithValidDefaults(registrationNumber = "a").errors should have length 2
    }

    "reject if more than max length" in {
      formWithValidDefaults(registrationNumber = "AB53WERT").errors should have length 1
    }

    "reject if more than max length 2" in {
      formWithValidDefaults(registrationNumber = "PJ056YYY").errors should have length 1
    }

    "reject if contains special characters" in {
      formWithValidDefaults(registrationNumber = "ab53ab%").errors should have length 1
    }

    "accept a selection of randomly generated vrms that all satisfy vrm regex" in {
      for (i <- 1 to 100) {
        val randomVrm = RandomVrmGenerator.vrm
        formWithValidDefaults(registrationNumber = randomVrm).get.registrationNumber should equal(randomVrm)
      }
    }
  }

  private val bruteForceServiceImpl: BruteForcePreventionService = {
    val bruteForcePreventionWebService: BruteForcePreventionWebService = mock[BruteForcePreventionWebService]
    when(bruteForcePreventionWebService.callBruteForce(anyString())).
      thenReturn( Future.successful( new FakeResponse(status = OK) ))

    new BruteForcePreventionServiceImpl(
      config = new BruteForcePreventionConfig,
      ws = bruteForcePreventionWebService,
      dateService = new FakeDateServiceImpl
    )
  }

  private def vehicleLookupResponseGenerator(fullResponse:(Int, Option[VehicleDetailsResponseDto])) = {
    val vehicleLookupWebService = mock[VehicleLookupWebService]

    when(vehicleLookupWebService.callVehicleLookupService(any[VehicleDetailsRequestDto], any[String])).
      thenReturn(Future.successful {
        val responseAsJson : Option[JsValue] = fullResponse._2 match {
          case Some(e) => Some(Json.toJson(e))
          case _ => None
        }
        new FakeResponse(status = fullResponse._1, fakeJson = responseAsJson)// Any call to a webservice will always return this successful response.
      })

    val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(vehicleLookupWebService)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
    new VehicleLookup(bruteForceService = bruteForceServiceImpl,
      vehicleLookupService = vehicleLookupServiceImpl, surveyUrl, dateService)
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
