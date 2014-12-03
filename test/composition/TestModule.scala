package composition

import com.google.inject.name.Names
import com.typesafe.config.ConfigFactory
import com.tzavellas.sse.guice.ScalaModule
import composition.DevModule.bind
import filters.{DateTimeZoneServiceImpl, DateTimeZoneService}
import org.joda.time.DateTimeZone
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter
import AccessLoggingFilter.AccessLoggerName
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, LoggerLike, Logger}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{NoCookieFlags, CookieFlags, ClientSideSessionFactory, ClearTextClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupWebService, AddressLookupService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.gds.AddressLookupServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionWebService, BruteForcePreventionServiceImpl, BruteForcePreventionService}
import webserviceclients.dispose.{DisposeWebService, DisposeServiceImpl, DisposeService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehiclelookup.{VehicleLookupWebService, VehicleLookupServiceImpl, VehicleLookupService}
import webserviceclients.fakes.FakeVehicleLookupWebService
import webserviceclients.fakes.FakeDisposeWebServiceImpl
import webserviceclients.fakes.FakeDateServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty

class TestModule() extends ScalaModule with MockitoSugar {
  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

    val applicationConf = System.getProperty("config.file", s"application.dev.conf")
    implicit val config = Configuration(ConfigFactory.load(applicationConf))

    getProperty("addressLookupService.type", "ordnanceSurvey") match {
      case "ordnanceSurvey" => ordnanceSurveyAddressLookup()
      case _ => gdsAddressLookup()
    }
    bind[VehicleLookupWebService].to[FakeVehicleLookupWebService].asEagerSingleton()
    bind[VehicleLookupService].to[VehicleLookupServiceImpl].asEagerSingleton()
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
