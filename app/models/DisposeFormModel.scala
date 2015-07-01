package models

import mappings.Consent.consent
import models.DisposeCacheKeyPrefix.CookiePrefix
import org.joda.time.LocalDate
import play.api.data.Mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.mappings.Mileage.mileage
import common.clientsidesession.CacheKey
import common.services.DateService
import common.mappings.Date.{dateMapping, notBefore, notInTheFuture}
import uk.gov.dvla.vehicles.presentation.common.mappings.Email._
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle

final case class DisposeFormModel(mileage: Option[Int],
                                  dateOfDisposal: LocalDate,
                                  consent: String,
                                  lossOfRegistrationConsent: String)

object DisposeFormModel {
  implicit val JsonFormat = Json.format[DisposeFormModel]
  final val DisposeFormModelCacheKey = s"${CookiePrefix}disposeForm"
  implicit val Key = CacheKey[DisposeFormModel](value = DisposeFormModelCacheKey)
  final val DisposeOccurredCacheKey = s"${CookiePrefix}disposeOccurredCacheKey"
  final val PreventGoingToDisposePageCacheKey = s"${CookiePrefix}preventGoingToDisposePage"
  final val DisposeFormTransactionIdCacheKey = s"${CookiePrefix}disposeFormTransactionId"
  final val DisposeFormTimestampIdCacheKey = s"${CookiePrefix}disposeFormTimestampId"
  final val DisposeFormRegistrationNumberCacheKey = s"${CookiePrefix}disposeFormRegistrationNumber"
  final val SurveyRequestTriggerDateCacheKey = s"${CookiePrefix}surveyRequestTriggerDate"

  object Form {
    final val MileageId = "mileage"
    final val DateOfDisposalId = "dateOfDisposal"
    final val ConsentId = "consent"
    final val LossOfRegistrationConsentId = "lossOfRegistrationConsent"
    final val DateOfDisposalYearsIntoThePast = 2
    final val TodaysDateOfDisposal = "todays_date"
    final val EmailId = "email"
    final val EmailOptionId = "option_email"
    final val BackId = "back"
    final val SubmitId = "submit"

    def mapping(implicit dateService: DateService): Mapping[DisposeFormModel] =
      play.api.data.Forms.mapping(
        MileageId -> mileage,
        DateOfDisposalId -> dateMapping.verifying(notInTheFuture()).
          verifying(notBefore(new LocalDate().minusYears(DateOfDisposalYearsIntoThePast))),
        ConsentId -> consent,
        LossOfRegistrationConsentId -> consent
      )(DisposeFormModel.apply)(DisposeFormModel.unapply)
  }
}

final case class DisposeFormModelPrivate(mileage: Option[Int],
                                  dateOfDisposal: LocalDate,
                                  email: Option[String],
                                  consent: String,
                                  lossOfRegistrationConsent: String)

object DisposeFormModelPrivate {
  implicit val JsonFormat = Json.format[DisposeFormModelPrivate]
  final val DisposeFormModelCacheKey = s"${CookiePrefix}disposeForm"
  implicit val Key = CacheKey[DisposeFormModel](value = DisposeFormModelCacheKey)
  final val DisposeOccurredCacheKey = s"${CookiePrefix}disposeOccurredCacheKey"
  final val PreventGoingToDisposePageCacheKey = s"${CookiePrefix}preventGoingToDisposePage"
  final val DisposeFormTransactionIdCacheKey = s"${CookiePrefix}disposeFormTransactionId"
  final val DisposeFormTimestampIdCacheKey = s"${CookiePrefix}disposeFormTimestampId"
  final val DisposeFormRegistrationNumberCacheKey = s"${CookiePrefix}disposeFormRegistrationNumber"
  final val SurveyRequestTriggerDateCacheKey = s"${CookiePrefix}surveyRequestTriggerDate"

  object Form {
    final val MileageId = "mileage"
    final val DateOfDisposalId = "dateOfDisposal"
    final val ConsentId = "consent"
    final val LossOfRegistrationConsentId = "lossOfRegistrationConsent"
    final val DateOfDisposalYearsIntoThePast = 2
    final val TodaysDateOfDisposal = "todays_date"
    final val EmailId = "email"
    final val EmailOptionId = "option_email"
    final val BackId = "back"
    final val SubmitId = "submit"

    def mapping(implicit dateService: DateService): Mapping[DisposeFormModelPrivate] =
      play.api.data.Forms.mapping(
        MileageId -> mileage,
        DateOfDisposalId -> dateMapping.verifying(notInTheFuture()).
          verifying(notBefore(new LocalDate().minusYears(DateOfDisposalYearsIntoThePast))),
        EmailOptionId -> OptionalToggle.optional(emailConfirm.withPrefix(EmailId)),
        ConsentId -> consent,
        LossOfRegistrationConsentId -> consent
      )(DisposeFormModelPrivate.apply)(DisposeFormModelPrivate.unapply)
  }
}