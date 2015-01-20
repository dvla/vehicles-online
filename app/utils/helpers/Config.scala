package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import common.webserviceclients.config.{ GDSAddressLookupConfig, OrdnanceSurveyConfig, VehicleLookupConfig}
import uk.gov.dvla.vehicles.presentation.common.services.SEND.{From, EmailConfiguration}
import webserviceclients.dispose.DisposeConfig

class Config {

  private final val NotFound = "NOT FOUND"
  private final val NotFoundLong = 9

//  lazy val vehiclesLookup = new VehicleLookupConfig
  lazy val ordnanceSurvey = new OrdnanceSurveyConfig
  lazy val gdsAddressLookup = new GDSAddressLookupConfig
  lazy val dispose = new DisposeConfig
  lazy val bruteForcePrevention = new BruteForcePreventionConfig

  // Micro-service config
//  lazy val vehicleLookupMicroServiceBaseUrl = vehiclesLookup.baseUrl

  lazy val ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  lazy val ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout
  lazy val ordnanceSurveyUseUprn: Boolean = getProperty[Boolean]("ordnancesurvey.useUprn")

  lazy val gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  lazy val gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  lazy val gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  lazy val disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  lazy val disposeMsRequestTimeout = dispose.requestTimeout

  // Web headers
  lazy val applicationCode: String = getOptionalProperty[String]("webHeader.applicationCode").getOrElse(NotFound)
  lazy val serviceTypeCode: String = getOptionalProperty[String]("webHeader.serviceTypeCode").getOrElse(NotFound)
  lazy val channelCode: String = getOptionalProperty[String]("webHeader.channelCode").getOrElse(NotFound)
  lazy val contactId: Long = getOptionalProperty[Long]("webHeader.contactId").getOrElse(NotFoundLong)

  // Brute force prevention config
  lazy val bruteForcePreventionExpiryHeader = bruteForcePrevention.expiryHeader
  lazy val bruteForcePreventionMicroServiceBaseUrl = bruteForcePrevention.baseUrl
  lazy val bruteForcePreventionTimeoutMillis = bruteForcePrevention.requestTimeoutMillis
  lazy val isBruteForcePreventionEnabled: Boolean = bruteForcePrevention.isEnabled
  lazy val bruteForcePreventionServiceNameHeader: String = bruteForcePrevention.nameHeader
  lazy val bruteForcePreventionMaxAttemptsHeader: Int = bruteForcePrevention.maxAttemptsHeader

  // Prototype message in html
  lazy val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Prototype survey URL
  lazy val prototypeSurveyUrl: String = getProperty[String]("survey.url") // could be optional
  lazy val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval")

  // Google analytics
  lazy val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.dispose")

  // Progress step indicator
  lazy val isProgressBarEnabled: Boolean = getProperty[Boolean]("progressBar.enabled")
  lazy val isHtml5ValidationEnabled: Boolean = getProperty[Boolean]("html5Validation.enabled")

  lazy val startUrl: String = getProperty[String]("start.page")
  lazy val endUrl: String = getProperty[String]("end.page")


  // opening and closing times
  lazy val opening: Int = getProperty[Int]("openingTime")
  lazy val closing: Int = getProperty[Int]("closingTime")

  // Web headers

  lazy val emailConfiguration: EmailConfiguration = EmailConfiguration(
    getProperty[String]("smtp.host"),
    getProperty[Int]("smtp.port"),
    getProperty[String]("smtp.user"),
    getProperty[String]("smtp.password"),
    From(getProperty[String]("email.senderAddress"), "DO-NOT-REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )
}
