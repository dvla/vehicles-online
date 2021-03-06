package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.controllers.VehicleLookupConfig
import common.services.SEND.EmailConfiguration
import common.utils.helpers.CommonConfig
import common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import webserviceclients.dispose.DisposeConfig

trait Config extends VehicleLookupConfig with CommonConfig {

  def assetsUrl: Option[String]

  def ordnanceSurveyConfig: OrdnanceSurveyConfig
  def disposeConfig: DisposeConfig

  // Micro-service config
  def ordnanceSurveyMicroServiceUrl: String
  def ordnanceSurveyRequestTimeout: Int

  def disposeVehicleMicroServiceBaseUrl: String
  def disposeMsRequestTimeout: Int

  // Web headers
  def applicationCode: String
  def vssServiceTypeCode: String
  def dmsServiceTypeCode: String
  def orgBusinessUnit: String
  def channelCode: String
  def contactId: Long

  // Prototype survey URL
  // TODO surveyUrl is optional in all other exemplars
  def surveyUrl: String
  def privateKeeperSurveyUrl: String
  def prototypeSurveyPrepositionInterval: Long

  // Google analytics
  def googleAnalyticsTrackingId: Option[String]

  def isHtml5ValidationEnabled: Boolean

  def startUrl: String

  // Opening and closing times
  def openingTimeMinOfDay: Int
  def closingTimeMinOfDay: Int
  def closingWarnPeriodMins: Int
  def closedDays: List[Int]

  def emailServiceMicroServiceUrlBase: String
  def emailServiceMsRequestTimeout: Int
  def emailConfiguration: EmailConfiguration
  def imagesPath: String
}