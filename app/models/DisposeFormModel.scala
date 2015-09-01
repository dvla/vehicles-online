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
import common.mappings.Email.emailConfirm
import common.mappings.OptionalToggle

trait DisposeFormModelBase {
  val mileage: Option[Int]
  val dateOfDisposal: LocalDate
  val consent: String
  val lossOfRegistrationConsent: String
}

object DisposeFormModelBase {
  object Form {
    final val MileageId = "mileage"
    final val DateOfDisposalId = "dateOfDisposal"
    final val ConsentId = "consent"
    final val LossOfRegistrationConsentId = "lossOfRegistrationConsent"
    final val DateOfDisposalYearsIntoThePast = 2
    final val TodaysDateOfDisposal = "todays_date"
    final val BackId = "back"
    final val SubmitId = "submit"
  }
}

final case class DisposeFormModel(mileage: Option[Int],
                                  dateOfDisposal: LocalDate,
                                  consent: String,
                                  lossOfRegistrationConsent: String) extends DisposeFormModelBase

final case class PrivateDisposeFormModel(mileage: Option[Int],
                                  dateOfDisposal: LocalDate,
                                  email: Option[String],
                                  consent: String,
                                  lossOfRegistrationConsent: String) extends DisposeFormModelBase

trait DisposeFormModelObjectBase {
  final val DisposeFormModelCacheKey = s"${CookiePrefix}disposeForm"
  final val DisposeOccurredCacheKey = s"${CookiePrefix}disposeOccurredCacheKey"
  final val PreventGoingToDisposePageCacheKey = s"${CookiePrefix}preventGoingToDisposePage"
  final val DisposeFormTransactionIdCacheKey = s"${CookiePrefix}disposeFormTransactionId"
  final val DisposeFormTimestampIdCacheKey = s"${CookiePrefix}disposeFormTimestampId"
  final val DisposeFormRegistrationNumberCacheKey = s"${CookiePrefix}disposeFormRegistrationNumber"
  final val SurveyRequestTriggerDateCacheKey = s"${CookiePrefix}surveyRequestTriggerDate"
}

object DisposeFormModel extends DisposeFormModelObjectBase {
  implicit val JsonFormat = Json.format[DisposeFormModel]
  implicit val Key = CacheKey[DisposeFormModel](value = DisposeFormModelCacheKey)

  object Form {
    def mapping(implicit dateService: DateService): Mapping[DisposeFormModel] =
      play.api.data.Forms.mapping(
        DisposeFormModelBase.Form.MileageId -> mileage,
        DisposeFormModelBase.Form.DateOfDisposalId -> dateMapping.verifying(notInTheFuture()).
          verifying(notBefore(new LocalDate().minusYears(DisposeFormModelBase.Form.DateOfDisposalYearsIntoThePast))),
        DisposeFormModelBase.Form.ConsentId -> consent,
        DisposeFormModelBase.Form.LossOfRegistrationConsentId -> consent
      )(DisposeFormModel.apply)(DisposeFormModel.unapply)
  }
}

object PrivateDisposeFormModel extends DisposeFormModelObjectBase {
  implicit val JsonFormat = Json.format[PrivateDisposeFormModel]
  final val PrivateDisposeFormModelCacheKey = s"${CookiePrefix}disposeFormPrivate"
  implicit val Key = CacheKey[PrivateDisposeFormModel](value = PrivateDisposeFormModelCacheKey)

  object Form {
    final val EmailId = "email"
    final val EmailOptionId = "option_email"

    def mapping(implicit dateService: DateService): Mapping[PrivateDisposeFormModel] =
      play.api.data.Forms.mapping(
        DisposeFormModelBase.Form.MileageId -> mileage,
        DisposeFormModelBase.Form.DateOfDisposalId -> dateMapping.verifying(notInTheFuture()).
          verifying(notBefore(new LocalDate().minusYears(DisposeFormModelBase.Form.DateOfDisposalYearsIntoThePast))),
        EmailOptionId -> OptionalToggle.optional(emailConfirm.withPrefix(EmailId)),
        DisposeFormModelBase.Form.ConsentId -> consent,
        DisposeFormModelBase.Form.LossOfRegistrationConsentId -> consent
      )(PrivateDisposeFormModel.apply)(PrivateDisposeFormModel.unapply)
  }
}
