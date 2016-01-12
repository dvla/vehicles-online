package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.booleanProp
import common.ConfigProperties.getProperty
import common.ConfigProperties.getDurationProperty
import common.ConfigProperties.getOptionalProperty
import common.ConfigProperties.getStringListProperty
import common.ConfigProperties.intProp
import common.ConfigProperties.longProp
import common.ConfigProperties.stringProp
import common.services.SEND.EmailConfiguration
import common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import common.webserviceclients.config.GDSAddressLookupConfig
import common.webserviceclients.emailservice.From
import webserviceclients.dispose.DisposeConfig

final class ConfigImpl extends Config {

  override def assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

//  override val vehiclesLookup = new VehicleLookupConfig
  override val ordnanceSurvey = new OrdnanceSurveyConfig
  override val gdsAddressLookup = new GDSAddressLookupConfig
  override val dispose = new DisposeConfig
  override val bruteForcePrevention = new BruteForcePreventionConfig

  // Micro-service config
//  override val vehicleLookupMicroServiceBaseUrl = vehiclesLookup.baseUrl

  override val ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  override val ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout

  override val gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  override val gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  override val gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  override val disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  override val disposeMsRequestTimeout = dispose.requestTimeout

  // Web headers
  override val applicationCode: String =
    getOptionalProperty[String]("webHeader.applicationCode").getOrElse(NotFound)
  override val vssServiceTypeCode: String =
    getOptionalProperty[String]("webHeader.vssServiceTypeCode").getOrElse(NotFound)
  override val dmsServiceTypeCode: String =
    getOptionalProperty[String]("webHeader.dmsServiceTypeCode").getOrElse(NotFound)
  override val channelCode: String =
    getOptionalProperty[String]("webHeader.channelCode").getOrElse(NotFound)
  override val contactId: Long =
    getOptionalProperty[Long]("webHeader.contactId").getOrElse(NotFoundLong)
  override val orgBusinessUnit: String =
    getOptionalProperty[String]("webHeader.orgBusinessUnit").getOrElse(NotFound)

  // Brute force prevention config
  override val bruteForcePreventionExpiryHeader = bruteForcePrevention.expiryHeader
  override val bruteForcePreventionMicroServiceBaseUrl = bruteForcePrevention.baseUrl
  override val bruteForcePreventionTimeoutMillis = bruteForcePrevention.requestTimeoutMillis
  override val isBruteForcePreventionEnabled: Boolean = bruteForcePrevention.isEnabled
  override val bruteForcePreventionServiceNameHeader: String = bruteForcePrevention.nameHeader
  override val bruteForcePreventionMaxAttemptsHeader: Int = bruteForcePrevention.maxAttemptsHeader

  // Prototype message in html
  override val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Prototype survey URL
  override val surveyUrl: String = getProperty[String]("survey.url")
  override val privateKeeperSurveyUrl: String = getProperty[String]("private.survey.url")
  override val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval")

  // Google analytics
  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.dispose")


  override val isHtml5ValidationEnabled: Boolean = getProperty[Boolean]("html5Validation.enabled")

  override val startUrl: String = getProperty[String]("start.page")

  // Opening and closing times
  override val openingTimeMinOfDay: Int = getProperty[Int]("openingTimeMinOfDay")
  override val closingTimeMinOfDay: Int = getProperty[Int]("closingTimeMinOfDay")

  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins").getOrElse(15)

  override val emailServiceMicroServiceUrlBase: String =
    getOptionalProperty[String]("emailServiceMicroServiceUrlBase").getOrElse(NotFound)
  override val emailServiceMsRequestTimeout: Int =
    getOptionalProperty[Int]("emailService.ms.requesttimeout").getOrElse(10000)

  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), "DO-NOT-REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )

  override val imagesPath: String = getProperty[String]("email.image.path")
}
