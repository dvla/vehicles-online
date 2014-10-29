package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.{getProperty, getDurationProperty}
import common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import common.webserviceclients.config.{ GDSAddressLookupConfig, OrdnanceSurveyConfig, VehicleLookupConfig}
import webserviceclients.dispose.DisposeConfig
import scala.concurrent.duration.DurationInt

class Config {
  val vehiclesLookup = new VehicleLookupConfig
  val ordnanceSurvey = new OrdnanceSurveyConfig
  val gdsAddressLookup = new GDSAddressLookupConfig
  val dispose = new DisposeConfig
  val bruteForcePrevention = new BruteForcePreventionConfig

  // Micro-service config
  val vehicleLookupMicroServiceBaseUrl = vehiclesLookup.baseUrl

  val ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  val ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout
  val ordnanceSurveyUseUprn: Boolean = getProperty("ordnancesurvey.useUprn", default = false)

  val gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  val gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  val gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  val disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  val disposeMsRequestTimeout = dispose.requestTimeout

  // Brute force prevention config
  val bruteForcePreventionExpiryHeader = bruteForcePrevention.expiryHeader
  val bruteForcePreventionMicroServiceBaseUrl = bruteForcePrevention.baseUrl
  val bruteForcePreventionTimeoutMillis = bruteForcePrevention.requestTimeoutMillis
  val isBruteForcePreventionEnabled: Boolean = bruteForcePrevention.isEnabled
  val bruteForcePreventionServiceNameHeader: String = bruteForcePrevention.nameHeader
  val bruteForcePreventionMaxAttemptsHeader: Int = bruteForcePrevention.maxAttemptsHeader

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean = getProperty("prototype.disclaimer", default = true)

  // Prototype survey URL
  val prototypeSurveyUrl: String = getProperty("survey.url", "")
  val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval", 7.days.toMillis)

  // Google analytics
  val isGoogleAnalyticsEnabled: Boolean = getProperty("googleAnalytics.enabled", default = false)
  val googleAnalyticsTrackingId: String = getProperty("googleAnalytics.id.dispose", "NOT FOUND")

  // Progress step indicator
  val isProgressBarEnabled: Boolean = getProperty("progressBar.enabled", default = true)
  val isHtml5ValidationEnabled: Boolean = getProperty("html5Validation.enabled", default = false)

  val startUrl: String = getProperty("start.page", default = "NOT FOUND")

  // opening and closing times
  val opening: Int = getProperty("openingTime", default = 1)
  val closing: Int = getProperty("closingTime", default = 23)
}