package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.{getProperty, getDurationProperty}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.{DisposeConfig, GDSAddressLookupConfig, OrdnanceSurveyConfig, VehicleLookupConfig}
import scala.concurrent.duration.DurationInt

class Config {
  val vehiclesLookup = new VehicleLookupConfig
  val ordnanceSurvey = new OrdnanceSurveyConfig
  val gdsAddressLookup = new GDSAddressLookupConfig
  val dispose = new DisposeConfig
  val brutForcePrevention = new BruteForcePreventionConfig

  // Micro-service config
  val vehicleLookupMicroServiceBaseUrl = vehiclesLookup.baseUrl

  val ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  val ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout

  val gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  val gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  val gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  val disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  val disposeMsRequestTimeout = dispose.requestTimeout

  // Brute force prevention config
  val bruteForcePreventionExpiryHeader = brutForcePrevention.expiryHeader
  val bruteForcePreventionMicroServiceBaseUrl = brutForcePrevention.baseUrl
  val bruteForcePreventionTimeout = brutForcePrevention.requestTimeout
  val isBruteForcePreventionEnabled: Boolean = brutForcePrevention.isEnabled
  val bruteForcePreventionServiceNameHeader: String = brutForcePrevention.nameHeader
  val bruteForcePreventionMaxAttemptsHeader: Int = brutForcePrevention.maxAttemptsHeader

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean = getProperty("prototype.disclaimer", default = true)

  // Prototype survey URL
  val prototypeSurveyUrl: String = getProperty("survey.url", "")
  val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval", 7.days.toMillis)

  // Google analytics
  val isGoogleAnalyticsEnabled: Boolean = getProperty("googleAnalytics.enabled", default = true)

  // Progress step indicator
  val isProgressBarEnabled: Boolean = getProperty("progressBar.enabled", default = true)
  val isHtml5ValidationEnabled: Boolean = getProperty("html5Validation.enabled", default = false)

  val startUrl: String = getProperty("start.page", default = "NOT FOUND")

  // opening and closing times
  val opening: Int = getProperty("openingTime", default = 0)
  val closing: Int = getProperty("closingTime", default = 24)
}