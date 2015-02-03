package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import play.api.{Logger, LoggerLike}
import services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.{getProperty, getOptionalProperty, stringProp, booleanProp}
import common.clientsidesession.AesEncryption
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieEncryption
import common.clientsidesession.CookieFlags
import common.clientsidesession.CookieFlagsFromConfig
import common.clientsidesession.CookieNameHashGenerator
import common.clientsidesession.EncryptedClientSideSessionFactory
import common.clientsidesession.Sha1HashGenerator
import common.filters.AccessLoggingFilter.AccessLoggerName
import common.filters.{DateTimeZoneServiceImpl, DateTimeZoneService}
import common.services.DateService
import common.webserviceclients.addresslookup.gds.{AddressLookupServiceImpl, WebServiceImpl}
import common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionServiceImpl
import common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupServiceImpl
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebServiceImpl
import utils.helpers.Config
import webserviceclients.dispose.{DisposeWebServiceImpl, DisposeWebService, DisposeServiceImpl, DisposeService}

/**
 * Provides real implementations of traits
 * Note the use of sse-guice, which is a library that makes the Guice internal DSL more scala friendly
 * eg we can write this:
 * bind[Service].to[ServiceImpl].in[Singleton]
 * instead of this:
 * bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *
 * Look in build.scala for where we import the sse-guice library
 */
object DevModule extends ScalaModule {
  def configure() {

    bind[Config].to[utils.helpers.ConfigImpl].asEagerSingleton()

    getProperty[String]("addressLookupService.type") match {
      case "ordnanceSurvey" =>
        bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl].asEagerSingleton()
        bind[AddressLookupWebService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.WebServiceImpl].asEagerSingleton()
      case _ =>
        bind[AddressLookupService].to[AddressLookupServiceImpl].asEagerSingleton()
        bind[AddressLookupWebService].to[WebServiceImpl].asEagerSingleton()
    }
    bind[VehicleAndKeeperLookupWebService].to[VehicleAndKeeperLookupWebServiceImpl].asEagerSingleton()
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[DisposeWebService].to[DisposeWebServiceImpl].asEagerSingleton()
    bind[DisposeService].to[DisposeServiceImpl].asEagerSingleton()
    bind[DateService].to[DateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[CookieFlagsFromConfig].asEagerSingleton()

    if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionWebService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.WebServiceImpl].asEagerSingleton()
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[DateTimeZoneService].toInstance(new DateTimeZoneServiceImpl)
  }
}