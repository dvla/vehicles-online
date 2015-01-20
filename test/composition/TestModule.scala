package composition

import com.google.inject.name.Names
import com.typesafe.config.ConfigFactory
import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, LoggerLike, Logger}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{NoCookieFlags, CookieFlags, ClientSideSessionFactory, ClearTextClientSideSessionFactory}
import common.ConfigProperties.getProperty
import common.filters.{DateTimeZoneServiceImpl, DateTimeZoneService, AccessLoggingFilter}
import AccessLoggingFilter.AccessLoggerName
import common.services.DateService
import common.webserviceclients.addresslookup.{AddressLookupWebService, AddressLookupService}
import common.webserviceclients.addresslookup.gds.AddressLookupServiceImpl
import common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionServiceImpl
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupServiceImpl
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import webserviceclients.dispose.{DisposeWebService, DisposeServiceImpl, DisposeService}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.FakeDateServiceImpl
import webserviceclients.fakes.FakeDisposeWebServiceImpl
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService

class TestModule() extends ScalaModule with MockitoSugar {
  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

    val applicationConf = System.getProperty("config.file", s"application.dev.conf")
    implicit val config = Configuration(ConfigFactory.load(applicationConf))

    getProperty[String]("addressLookupService.type") match {
      case "ordnanceSurvey" => ordnanceSurveyAddressLookup()
      case _ => gdsAddressLookup()
    }
//    bind[VehicleLookupWebService].to[FakeVehicleLookupWebService].asEagerSingleton() // TODO deleteme
//    bind[VehicleLookupService].to[VehicleLookupServiceImpl].asEagerSingleton() // TODO deleteme
    bind[VehicleAndKeeperLookupWebService].to[FakeVehicleAndKeeperLookupWebService].asEagerSingleton()
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()

    bind[DisposeWebService].to[FakeDisposeWebServiceImpl].asEagerSingleton()
    bind[DisposeService].to[DisposeServiceImpl].asEagerSingleton()
    bind[DateService].to[FakeDateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionWebService].to[FakeBruteForcePreventionWebServiceImpl].asEagerSingleton()
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[DateTimeZoneService].toInstance(new DateTimeZoneServiceImpl)
  }


  private def ordnanceSurveyAddressLookup() = {
    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]

    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress,
      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
    )
    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
  }

  private def gdsAddressLookup() = {
    bind[AddressLookupService].to[AddressLookupServiceImpl]
    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForGdsAddressLookup,
      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForGdsAddressLookup
    )
    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
  }
}
