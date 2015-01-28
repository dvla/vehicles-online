package models

import mappings.Consent
import Consent.consent
import play.api.data.Mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.mappings.DayMonthYear.dayMonthYear
import common.mappings.Mileage.mileage
import common.views.constraints.DayMonthYear.{after, notInFuture, validDate}
import common.clientsidesession.CacheKey
import common.services.DateService
import common.views.models.DayMonthYear

final case class DisposeFormModel(mileage: Option[Int],
                                  dateOfDisposal: DayMonthYear,
                                  consent: String,
                                  lossOfRegistrationConsent: String)

object DisposeFormModel {
  implicit val JsonFormat = Json.format[DisposeFormModel]
  final val DisposeFormModelCacheKey = s"${CacheKeyPrefix}disposeForm"
  implicit val Key = CacheKey[DisposeFormModel](value = DisposeFormModelCacheKey)
  final val DisposeOccurredCacheKey = s"${CacheKeyPrefix}disposeOccurredCacheKey"
  final val PreventGoingToDisposePageCacheKey = s"${CacheKeyPrefix}preventGoingToDisposePage"
  final val DisposeFormTransactionIdCacheKey = s"${CacheKeyPrefix}disposeFormTransactionId"
  final val DisposeFormTimestampIdCacheKey = s"${CacheKeyPrefix}disposeFormTimestampId"
  final val DisposeFormRegistrationNumberCacheKey = s"${CacheKeyPrefix}disposeFormRegistrationNumber"
  final val SurveyRequestTriggerDateCacheKey = s"${CacheKeyPrefix}surveyRequestTriggerDate"

  object Form {
    final val MileageId = "mileage"
    final val DateOfDisposalId = "dateOfDisposal"
    final val ConsentId = "consent"
    final val LossOfRegistrationConsentId = "lossOfRegistrationConsent"
    final val DateOfDisposalYearsIntoThePast = 2
    final val TodaysDateOfDisposal = "todays_date"
    final val BackId = "back"
    final val SubmitId = "submit"

    def mapping(dateService: DateService): Mapping[DisposeFormModel] =
      play.api.data.Forms.mapping(
        MileageId -> mileage,
        DateOfDisposalId -> dayMonthYear.verifying(validDate(),
          after(earliest = (dateService.today - DateOfDisposalYearsIntoThePast).years),
          notInFuture(dateService)),
        ConsentId -> consent,
        LossOfRegistrationConsentId -> consent
      )(DisposeFormModel.apply)(DisposeFormModel.unapply)
  }
}
