package models

import uk.gov.dvla.vehicles.presentation.common.mappings.Consent.consent
import models.DisposeCacheKeyPrefix.CookiePrefix
import org.joda.time.LocalDate
import play.api.data.Mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Date.{dateMapping, notBefore, notInTheFuture}
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.emailConfirm
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle
import uk.gov.dvla.vehicles.presentation.common.services.DateService

trait DisposeFormModelBase {
  val mileage: Option[Int]
  val dateOfDisposal: LocalDate
  val email: Option[String]
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
    final val EmailId = "email"
    final val EmailOptionId = "option_email"
  }
}

final case class DisposeFormModel(mileage: Option[Int],
                                  dateOfDisposal: LocalDate,
                                  email: Option[String],
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
        DisposeFormModelBase.Form.EmailOptionId -> OptionalToggle.optional(emailConfirm.withPrefix(DisposeFormModelBase.Form.EmailId)),
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

    def mapping(implicit dateService: DateService): Mapping[PrivateDisposeFormModel] =
      play.api.data.Forms.mapping(
        DisposeFormModelBase.Form.MileageId -> mileage,
        DisposeFormModelBase.Form.DateOfDisposalId -> dateMapping.verifying(notInTheFuture()).
          verifying(notBefore(new LocalDate().minusYears(DisposeFormModelBase.Form.DateOfDisposalYearsIntoThePast))),
        DisposeFormModelBase.Form.EmailOptionId -> OptionalToggle.optional(emailConfirm.withPrefix(DisposeFormModelBase.Form.EmailId)),
        DisposeFormModelBase.Form.ConsentId -> consent,
        DisposeFormModelBase.Form.LossOfRegistrationConsentId -> consent
      )(PrivateDisposeFormModel.apply)(PrivateDisposeFormModel.unapply)
  }
}
