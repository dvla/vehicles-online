package composition

import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.services.SEND.{EmailConfiguration, From}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.ordnance_survey.TestOrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.{GDSAddressLookupConfig, OrdnanceSurveyConfig, VehicleLookupConfig}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicle_lookup.TestVehicleLookupConfig
import webserviceclients.dispose.DisposeConfig
import utils.helpers.Config

class TestConfig extends Config {
//  private val notFound = "NOT FOUND"

  override lazy val vehiclesLookup = new VehicleLookupConfig {
    override lazy val baseUrl = "/"
  }
//  override lazy val ordnanceSurvey = new TestOrdnanceSurveyConfig
//  override lazy val gdsAddressLookup = new GDSAddressLookupConfig
//  override lazy val dispose = new DisposeConfig
//  override lazy val bruteForcePrevention = new BruteForcePreventionConfig

  // Micro-service config
//  override lazy val vehicleLookupMicroServiceBaseUrl = vehiclesLookup.baseUrl
//
//  override lazy val ordnanceSurveyMicroServiceUrl = ordnanceSurvey.baseUrl
//  override lazy val ordnanceSurveyRequestTimeout = ordnanceSurvey.requestTimeout
  override lazy val ordnanceSurveyUseUprn: Boolean = false

//  override lazy val gdsAddressLookupBaseUrl = gdsAddressLookup.baseUrl
//  override lazy val gdsAddressLookupRequestTimeout = gdsAddressLookup.requestTimeout
//  override lazy val gdsAddressLookupAuthorisation = gdsAddressLookup.authorisation
//
//  override lazy val disposeVehicleMicroServiceBaseUrl = dispose.baseUrl
//  override lazy val disposeMsRequestTimeout = dispose.requestTimeout

  // Brute force prevention config
//  override lazy val bruteForcePreventionExpiryHeader = bruteForcePrevention.expiryHeader
//  override lazy val bruteForcePreventionMicroServiceBaseUrl = bruteForcePrevention.baseUrl
//  override lazy val bruteForcePreventionTimeoutMillis = bruteForcePrevention.requestTimeoutMillis
//  override lazy val isBruteForcePreventionEnabled: Boolean = bruteForcePrevention.isEnabled
//  override lazy val bruteForcePreventionServiceNameHeader: String = bruteForcePrevention.nameHeader
//  override lazy val bruteForcePreventionMaxAttemptsHeader: Int = bruteForcePrevention.maxAttemptsHeader

  // Prototype message in html
  override lazy val isPrototypeBannerVisible: Boolean = true

  // Prototype survey URL
  override lazy val prototypeSurveyUrl: String = "" // could be optional
  override lazy val prototypeSurveyPrepositionInterval: Long = 1000000000000L

  // Google analytics
  override lazy val googleAnalyticsTrackingId: Option[String] = None

  // Progress step indicator
  override lazy val isProgressBarEnabled: Boolean = getOptionalProperty[Boolean]("progressBar.enabled").getOrElse(true)

  override lazy val isHtml5ValidationEnabled: Boolean =
    getOptionalProperty[Boolean]("html5Validation.enabled").getOrElse(false)

  override lazy val startUrl: String = "/before-you-start"
  override lazy val endUrl: String = "/before-you-start"

  // opening and closing times
  override lazy val opening: Int = 1
  override lazy val closing: Int = 18



  override lazy val emailConfiguration: EmailConfiguration = EmailConfiguration(
    "",
    25,
    "",
    "",
    From("", "DO-NOT-REPLY"),
    From("", "Feedback"),
    None
  )

}
