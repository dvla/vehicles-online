package composition

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.services.SEND.{EmailConfiguration, From}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.{GDSAddressLookupConfig, OrdnanceSurveyConfig, VehicleLookupConfig}
import utils.helpers.Config
import webserviceclients.dispose.DisposeConfig
import scala.concurrent.duration.DurationInt

class TestConfig extends Config {
//  private val notFound = "NOT FOUND"

  override lazy val vehiclesLookup = new VehicleLookupConfig {
    override lazy val baseUrl = "/"
  }
  override lazy val ordnanceSurvey = new TestOrdnanceSurveyConfig
  override lazy val gdsAddressLookup = new GDSAddressLookupConfig
  override lazy val dispose = new DisposeConfig
  override lazy val bruteForcePrevention = new BruteForcePreventionConfig

  // Micro-service config
  def vehicleLookupMicroServiceBaseUrl = vehiclesLookup.baseUrl

  def ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
  def ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout
  def ordnanceSurveyUseUprn: Boolean = false

  def gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
  def gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
  def gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation

  def disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
  def disposeMsRequestTimeout = dispose.requestTimeout

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
    "",
    25,
    "",
    "",
    From("", "DO-NOT-REPLY"),
    From("", "Feedback"),
    None
  )

}

class TestOrdnanceSurveyConfig extends OrdnanceSurveyConfig {
  override lazy val baseUrl = ""
  override lazy val requestTimeout = 5.seconds.toMillis.toInt

}
