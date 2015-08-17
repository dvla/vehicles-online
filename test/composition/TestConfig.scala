package composition

import org.mockito.Mockito._
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{booleanProp, getOptionalProperty, intProp}
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.gds.FakeGDSAddressLookupConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.ordnance_survey.FakeOrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.brute_force_prevention.FakeBruteForcePreventionConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import utils.helpers.Config
import webserviceclients.dispose_service.FakeDisposeConfig

class TestConfig extends Config {

  override def assetsUrl: Option[String] = None

  override lazy val ordnanceSurvey = new FakeOrdnanceSurveyConfig
  override lazy val gdsAddressLookup = new FakeGDSAddressLookupConfig
  override lazy val dispose = new FakeDisposeConfig
  override lazy val bruteForcePrevention = new FakeBruteForcePreventionConfig

  // Micro-service config
  def ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  def ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout
  def ordnanceSurveyUseUprn: Boolean = false

  def gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  def gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  def gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  def disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  def disposeMsRequestTimeout = dispose.requestTimeout

  // Web headers
  def applicationCode = "WEBDTT"
  def vssServiceTypeCode = "WEBDTT"
  def dmsServiceTypeCode = "E"
  def channelCode = "WEBDTT"
  def contactId = 1
  def orgBusinessUnit = "WEBDTT"

  //Brute force prevention config
  def bruteForcePreventionExpiryHeader = bruteForcePrevention.expiryHeader
  def bruteForcePreventionMicroServiceBaseUrl = bruteForcePrevention.baseUrl
  def bruteForcePreventionTimeoutMillis = bruteForcePrevention.requestTimeoutMillis
  def isBruteForcePreventionEnabled: Boolean = bruteForcePrevention.isEnabled
  def bruteForcePreventionServiceNameHeader: String = bruteForcePrevention.nameHeader
  def bruteForcePreventionMaxAttemptsHeader: Int = bruteForcePrevention.maxAttemptsHeader

  // Prototype message in html
  def isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(true)

  // Prototype survey URL
  def surveyUrl: String = "" // could be optional
  def privateKeeperSurveyUrl = ""
  def prototypeSurveyPrepositionInterval: Long = 1000000000000L

  // Google analytics
  def googleAnalyticsTrackingId: Option[String] = None

  // Progress step indicator
  def isProgressBarEnabled: Boolean = getOptionalProperty[Boolean]("progressBar.enabled").getOrElse(true)

  def isHtml5ValidationEnabled: Boolean =
    getOptionalProperty[Boolean]("html5Validation.enabled").getOrElse(false)

  def startUrl: String = "/sell-to-the-trade/before-you-start"
  def endUrl: String = "/sell-to-the-trade/before-you-start"

  // Opening and closing times
  def openingTimeMinOfDay: Int = getOptionalProperty[Int]("openingTimeMinOfDay").getOrElse(0)
  def closingTimeMinOfDay: Int = getOptionalProperty[Int]("closingTimeMinOfDay").getOrElse(1440)

  def closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins").getOrElse(0)

  def emailServiceMicroServiceUrlBase: String = NotFound
  def emailServiceMsRequestTimeout: Int = 10000

  def emailConfiguration: EmailConfiguration = EmailConfiguration(
    from = From("", "DO-NOT-REPLY"),
    feedbackEmail = From("", "Feedback"),
    whiteList = None
  )

  def imagesPath: String = ""
}
