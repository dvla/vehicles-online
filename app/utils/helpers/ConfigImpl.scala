package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.booleanProp
import common.ConfigProperties.getProperty
import common.ConfigProperties.getDurationProperty
import common.ConfigProperties.getIntListProperty
import common.ConfigProperties.getOptionalProperty
import common.ConfigProperties.getStringListProperty
import common.ConfigProperties.intProp
import common.ConfigProperties.longProp
import common.ConfigProperties.stringProp
import common.services.SEND.EmailConfiguration
import common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import common.webserviceclients.emailservice.From
import webserviceclients.dispose.DisposeConfig

final class ConfigImpl extends Config {

  import ConfigImpl._

  override def assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

  override val ordnanceSurveyConfig = new OrdnanceSurveyConfig
  override val disposeConfig = new DisposeConfig

  // Micro-service config
  override val ordnanceSurveyMicroServiceUrl = ordnanceSurveyConfig.baseUrl
  override val ordnanceSurveyRequestTimeout = ordnanceSurveyConfig.requestTimeout

  override val disposeVehicleMicroServiceBaseUrl = disposeConfig.baseUrl
  override val disposeMsRequestTimeout = disposeConfig.requestTimeout

  // Web headers
  override val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  override val vssServiceTypeCode: String = getProperty[String]("webHeader.vssServiceTypeCode")
  override val dmsServiceTypeCode: String = getProperty[String]("webHeader.dmsServiceTypeCode")
  override val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  override val channelCode: String = getProperty[String]("webHeader.channelCode")
  override val contactId: Long = getProperty[Long]("webHeader.contactId")

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
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins")
    .getOrElse(DefaultClosingWarnPeriodMins)
  override val closedDays: List[Int] = getIntListProperty("closedDays").getOrElse(List())

  override val emailServiceMicroServiceUrlBase: String = getProperty[String]("emailServiceMicroServiceUrlBase")
  override val emailServiceMsRequestTimeout: Int =
    getOptionalProperty[Int]("emailService.ms.requesttimeout").getOrElse(DefaultEmailRequestTimeoutMillis)

  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), EmailFromName),
    From(getProperty[String]("email.feedbackAddress"), EmailFeedbackFromName),
    getStringListProperty("email.whitelist")
  )

  override val imagesPath: String = getProperty[String]("email.image.path")
}

object ConfigImpl {
  final val EmailFromName = "DO-NOT-REPLY"
  final val EmailFeedbackFromName = "Feedback"

  final val DefaultClosingWarnPeriodMins = 15
  final val DefaultEncryptCookies = true
  final val DefaultEmailRequestTimeoutMillis = 10000
}