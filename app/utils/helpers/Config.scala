package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import common.webserviceclients.config.{GDSAddressLookupConfig, OrdnanceSurveyConfig}
import common.services.SEND.EmailConfiguration
import webserviceclients.dispose.DisposeConfig

trait Config {

  final val NotFound = "NOT FOUND"
  final val NotFoundLong = 9

  def ordnanceSurvey: OrdnanceSurveyConfig
  def gdsAddressLookup: GDSAddressLookupConfig
  def dispose: DisposeConfig
  def bruteForcePrevention: BruteForcePreventionConfig

  // Micro-service config
  def ordnanceSurveyMicroServiceUrl: String
  def ordnanceSurveyRequestTimeout: Int
  def ordnanceSurveyUseUprn: Boolean

  def gdsAddressLookupBaseUrl: String
  def gdsAddressLookupRequestTimeout: Int
  def gdsAddressLookupAuthorisation: String

  def disposeVehicleMicroServiceBaseUrl: String
  def disposeMsRequestTimeout: Int

  // Web headers
  def applicationCode: String
  def serviceTypeCode: String
  def channelCode: String
  def contactId: Long

  // Brute force prevention config
  def bruteForcePreventionExpiryHeader: String
  def bruteForcePreventionMicroServiceBaseUrl: String
  def bruteForcePreventionTimeoutMillis: Int
  def isBruteForcePreventionEnabled: Boolean
  def bruteForcePreventionServiceNameHeader: String
  def bruteForcePreventionMaxAttemptsHeader: Int

  // Prototype message in html
  def isPrototypeBannerVisible: Boolean

  // Prototype survey URL
  def prototypeSurveyUrl: String
  def prototypeSurveyPrepositionInterval: Long

  // Google analytics
  def googleAnalyticsTrackingId: Option[String]

  // Progress step indicator
  def isProgressBarEnabled: Boolean
  def isHtml5ValidationEnabled: Boolean

  def startUrl: String
  def endUrl: String

  // opening and closing times
  def opening: Int
  def closing: Int

  // Web headers
  def emailConfiguration: EmailConfiguration
}
