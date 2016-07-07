package composition

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{booleanProp, getIntListProperty, getOptionalProperty, intProp}
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.gds.FakeGDSAddressLookupConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.ordnance_survey.FakeOrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import utils.helpers.Config
import webserviceclients.dispose_service.FakeDisposeConfig

final class TestConfig extends Config {

  override def assetsUrl: Option[String] = None

  override val ordnanceSurvey = new FakeOrdnanceSurveyConfig
  override val gdsAddressLookup = new FakeGDSAddressLookupConfig
  override val dispose = new FakeDisposeConfig

  // Micro-service config
  override val ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  override val ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout

  override val gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  override val gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  override val gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  override val disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  override val disposeMsRequestTimeout = dispose.requestTimeout

  // Web headers
  override val applicationCode = TestConfig.WEB_APPLICATION_CODE
  override val vssServiceTypeCode = TestConfig.WEB_VSSSERVICETYPE_CODE
  override val dmsServiceTypeCode = TestConfig.WEB_DMSSERVICETYPE_CODE
  override val channelCode = TestConfig.WEB_CHANNEL_CODE
  override val contactId = TestConfig.WEB_CONTACT_ID
  override val orgBusinessUnit = TestConfig.WEB_ORG_BU

  // Prototype message in html - declared as def because overriden in test MainUiSpec
  def isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(TestConfig.DEFAULT_PB_VISIBLE)

  // Prototype survey URL
  override val surveyUrl = ""
  override val privateKeeperSurveyUrl = ""
  override val prototypeSurveyPrepositionInterval: Long = TestConfig.VERY_LONG_SURVEY_INTERVAL

  // Google analytics
  override val googleAnalyticsTrackingId: Option[String] = None

  override val isHtml5ValidationEnabled: Boolean =
    getOptionalProperty[Boolean]("html5Validation.enabled").getOrElse(TestConfig.DEFAULT_HTML_VALIDATION)

  override val startUrl: String = TestConfig.START_URL

  // Opening and closing times
  override val openingTimeMinOfDay: Int = getOptionalProperty[Int]("openingTimeMinOfDay").getOrElse(TestConfig.DEFAULT_OPENING_TIME)
  override val closingTimeMinOfDay: Int = getOptionalProperty[Int]("closingTimeMinOfDay").getOrElse(TestConfig.DEFAULT_CLOSING_TIME)
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins").getOrElse(TestConfig.DEFAULT_CLOSING_WARN_PERIOD)
  override val closedDays: List[Int] = getIntListProperty("closedDays").getOrElse(List())

  override val emailServiceMicroServiceUrlBase: String = TestConfig.DEFAULT_BASE_URL
  override val emailServiceMsRequestTimeout: Int = TestConfig.DEFAULT_EMAIL_REQUEST_TIMEOUT

  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    from = From(TestConfig.EMAIL_FROM_EMAIL, TestConfig.EMAIL_FROM_NAME),
    feedbackEmail = From(TestConfig.EMAILFEEDBACK_FROM_EMAIL, TestConfig.EMAILFEEDBACK_FROM_NAME),
    whiteList = None
  )


  override val imagesPath: String = TestConfig.IMAGES_PATH
}

// placeholder for defaults and fixed test data
object TestConfig {
  final val EMAIL_FROM_NAME = "Someone"
  final val EMAILFEEDBACK_FROM_NAME = "Nobody"
  final val EMAIL_FROM_EMAIL = ""
  final val EMAILFEEDBACK_FROM_EMAIL = ""

  final val WEB_APPLICATION_CODE = "WEBDTT"
  final val WEB_VSSSERVICETYPE_CODE = "WEBDTT"
  final val WEB_DMSSERVICETYPE_CODE = "E"
  final val WEB_CHANNEL_CODE = "WEBDTT"
  final val WEB_CONTACT_ID = 1L
  final val WEB_ORG_BU = "WEBDTT"
  final val VERY_LONG_SURVEY_INTERVAL = 1000000000000L // in millis (approx 11574 days!)

  final val START_URL = "/sell-to-the-trade/before-you-start"
  final val IMAGES_PATH = ""

  final val DEFAULT_HTML_VALIDATION = false
  final val DEFAULT_PB_VISIBLE = true
  final val DEFAULT_OPENING_TIME = 0
  final val DEFAULT_CLOSING_TIME = 1440
  final val DEFAULT_CLOSING_WARN_PERIOD = 0

  final val DEFAULT_APPLICATION_CONTEXT = ""

  final val DEFAULT_EMAIL_REQUEST_TIMEOUT = 10000

  final val DEFAULT_BASE_URL = "NOT FOUND"

}