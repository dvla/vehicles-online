package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.{getProperty, getDurationProperty, getOptionalProperty, getStringListProperty}
import common.services.SEND.{EmailConfiguration, From}
import common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import common.webserviceclients.config.{GDSAddressLookupConfig, OrdnanceSurveyConfig}
import webserviceclients.dispose.DisposeConfig

final class ConfigImpl extends Config{

//  override val vehiclesLookup = new VehicleLookupConfig
  override val ordnanceSurvey = new OrdnanceSurveyConfig
  override val gdsAddressLookup = new GDSAddressLookupConfig
  override val dispose = new DisposeConfig
  override val bruteForcePrevention = new BruteForcePreventionConfig

  // Micro-service config
//  override val vehicleLookupMicroServiceBaseUrl = vehiclesLookup.baseUrl

  override val ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  override val ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout
  override val ordnanceSurveyUseUprn: Boolean = getProperty[Boolean]("ordnancesurvey.useUprn")

  override val gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  override val gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  override val gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  override val disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  override val disposeMsRequestTimeout = dispose.requestTimeout

  // Web headers
  override val applicationCode: String = getOptionalProperty[String]("webHeader.applicationCode").getOrElse(NotFound)
  override val serviceTypeCode: String = getOptionalProperty[String]("webHeader.serviceTypeCode").getOrElse(NotFound)
  override val channelCode: String = getOptionalProperty[String]("webHeader.channelCode").getOrElse(NotFound)
  override val contactId: Long = getOptionalProperty[Long]("webHeader.contactId").getOrElse(NotFoundLong)

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
  override val prototypeSurveyUrl: String = getProperty[String]("survey.url") // could be optional
  override val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval")

  // Google analytics
  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.dispose")

  // Progress step indicator
  override val isProgressBarEnabled: Boolean = getProperty[Boolean]("progressBar.enabled")
  override val isHtml5ValidationEnabled: Boolean = getProperty[Boolean]("html5Validation.enabled")

  override val startUrl: String = getProperty[String]("start.page")
  override val endUrl: String = getProperty[String]("end.page")


  // opening and closing times
  override val opening: Int = getProperty[Int]("openingTime")
  override val closing: Int = getProperty[Int]("closingTime")

  // Web headers

  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    getProperty[String]("smtp.host"),
    getProperty[Int]("smtp.port"),
    getProperty[String]("smtp.user"),
    getProperty[String]("smtp.password"),
    From(getProperty[String]("email.senderAddress"), "DO-NOT-REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )
}
