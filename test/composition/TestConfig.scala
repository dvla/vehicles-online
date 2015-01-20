package composition

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.services.SEND.{EmailConfiguration, From}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.gds.FakeGDSAddressLookupConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.brute_force_prevention.FakeBruteForcePreventionConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.ordnance_survey.FakeOrdnanceSurveyConfig
import utils.helpers.Config
import webserviceclients.dispose_service.FakeDisposeConfig

class TestConfig extends Config {

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
  def serviceTypeCode = "WEBDTT"
  def channelCode = "WEBDTT"
  def contactId = 1

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
  def prototypeSurveyUrl: String = "" // could be optional
  def prototypeSurveyPrepositionInterval: Long = 1000000000000L

  // Google analytics
  def googleAnalyticsTrackingId: Option[String] = None

  // Progress step indicator
  def isProgressBarEnabled: Boolean = getOptionalProperty[Boolean]("progressBar.enabled").getOrElse(true)

  def isHtml5ValidationEnabled: Boolean =
    getOptionalProperty[Boolean]("html5Validation.enabled").getOrElse(false)

  def startUrl: String = "/sell-to-the-trade/before-you-start"
  def endUrl: String = "/sell-to-the-trade/before-you-start"

  // opening and closing times
  def opening: Int = 1
  def closing: Int = 18

  def emailConfiguration: EmailConfiguration = EmailConfiguration(
    host = "",
    port = 25,
    username = "",
    password = "",
    from = From("", "DO-NOT-REPLY"),
    feedbackEmail = From("", "Feedback"),
    whiteList = None
  )
}
