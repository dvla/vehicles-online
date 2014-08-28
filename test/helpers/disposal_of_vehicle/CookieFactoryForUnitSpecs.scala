package helpers.disposal_of_vehicle

import composition.TestComposition
import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel, AddressModel, BruteForcePreventionModel}
import BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import org.joda.time.DateTime
import pages.disposal_of_vehicle.{HelpPage, VehicleLookupPage}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSession, ClientSideSessionFactory, CookieFlags}
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel, DayMonthYear}
import viewmodels.BusinessChooseYourAddressViewModel.BusinessChooseYourAddressCacheKey
import viewmodels.DisposeFormViewModel.SurveyRequestTriggerDateCacheKey
import viewmodels.DisposeFormViewModel.DisposeFormModelCacheKey
import viewmodels.DisposeFormViewModel.DisposeFormRegistrationNumberCacheKey
import viewmodels.DisposeFormViewModel.DisposeFormTimestampIdCacheKey
import viewmodels.DisposeFormViewModel.DisposeFormTransactionIdCacheKey
import viewmodels.DisposeFormViewModel.DisposeOccurredCacheKey
import viewmodels.DisposeFormViewModel.PreventGoingToDisposePageCacheKey
import viewmodels.EnterAddressManuallyViewModel.EnterAddressManuallyCacheKey
import viewmodels.HelpCacheKey
import viewmodels.SetupTradeDetailsViewModel.SetupTradeDetailsCacheKey
import TraderDetailsModel.TraderDetailsCacheKey
import VehicleDetailsModel.VehicleLookupDetailsCacheKey
import viewmodels.VehicleLookupFormViewModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import viewmodels.{BusinessChooseYourAddressViewModel, DisposeFormViewModel}
import viewmodels.{EnterAddressManuallyViewModel, SeenCookieMessageCacheKey, SetupTradeDetailsViewModel, VehicleLookupFormViewModel}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid}
import webserviceclients.fakes.FakeAddressLookupService.{PostTownValid, PostcodeValid, TraderBusinessNameValid}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import webserviceclients.fakes.FakeDisposeWebServiceImpl.TransactionIdValid
import webserviceclients.fakes.FakeVehicleLookupWebService.{KeeperNameValid, ReferenceNumberValid}
import webserviceclients.fakes.FakeVehicleLookupWebService.{RegistrationNumberValid, VehicleModelValid}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import webserviceclients.fakes.{FakeDateServiceImpl, FakeDisposeWebServiceImpl, FakeVehicleLookupWebService}

object CookieFactoryForUnitSpecs extends TestComposition { // TODO can we make this more fluent by returning "this" at the end of the defs

  implicit private val cookieFlags = injector.getInstance(classOf[CookieFlags])
  final val TrackingIdValue = "trackingId"
  private val session = new ClearTextClientSideSession(TrackingIdValue)

  private def createCookie[A](key: String, value: A)(implicit tjs: Writes[A]): Cookie = {
    val json = Json.toJson(value).toString()
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, json)
  }

  private def createCookie[A](key: String, value: String): Cookie = {
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, value)
  }

  def seenCookieMessage(): Cookie = {
    val key = SeenCookieMessageCacheKey
    val value = "yes" // TODO make a constant
    createCookie(key, value)
  }

  def setupTradeDetails(traderPostcode: String = PostcodeValid): Cookie = {
    val key = SetupTradeDetailsCacheKey
    val value = SetupTradeDetailsViewModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode)
    createCookie(key, value)
  }

  def businessChooseYourAddress(): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressViewModel(uprnSelected = traderUprnValid.toString)
    createCookie(key, value)
  }

  def enterAddressManually(): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyViewModel(
      addressAndPostcodeModel = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = BuildingNameOrNumberValid,
            line2 = Some(Line2Valid),
            line3 = Some(Line3Valid),
            postTown = PostTownValid
        )
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModel(uprn: Option[Long] = None,
                         buildingNameOrNumber: String = BuildingNameOrNumberValid,
                         line2: String = Line2Valid,
                         line3: String = Line3Valid,
                         postTown: String = PostTownValid,
                         traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, line2, line3, postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModelBuildingNameOrNumber(uprn: Option[Long] = None,
                                             buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                             postTown: String = PostTownValid,
                                             traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModelLine2(uprn: Option[Long] = None,
                              buildingNameOrNumber: String = BuildingNameOrNumberValid,
                              line2: String = Line2Valid,
                              postTown: String = PostTownValid,
                              traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, line2, postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModelPostTown(uprn: Option[Long] = None,
                                 postTown: String = PostTownValid,
                                 traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(uprn = uprn, address = Seq(postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString): Cookie = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology = dateTimeISOChronology
    )
    createCookie(key, value)
  }

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid): Cookie = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormViewModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber
    )
    createCookie(key, value)
  }

  def vehicleDetailsModel(registrationNumber: String = RegistrationNumberValid,
                          vehicleMake: String = FakeVehicleLookupWebService.VehicleMakeValid,
                          vehicleModel: String = VehicleModelValid,
                          keeperName: String = KeeperNameValid): Cookie = {
    val key = VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(
      registrationNumber = registrationNumber,
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel,
      disposeFlag = true
    )
    createCookie(key, value)
  }

  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure"): Cookie =
    createCookie(VehicleLookupResponseCodeCacheKey, responseCode)

  def disposeFormModel(mileage: Option[Int] = None): Cookie = {
    val key = DisposeFormModelCacheKey
    val value = DisposeFormViewModel(
      mileage = mileage,
      dateOfDisposal = DayMonthYear(
        FakeDateServiceImpl.DateOfDisposalDayValid.toInt,
        FakeDateServiceImpl.DateOfDisposalMonthValid.toInt, FakeDateServiceImpl.DateOfDisposalYearValid.toInt
      ),
      consent = FakeDisposeWebServiceImpl.ConsentValid,
      lossOfRegistrationConsent = FakeDisposeWebServiceImpl.ConsentValid
    )
    createCookie(key, value)
  }

  def trackingIdModel(value: String = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, value)
  }

  def disposeFormRegistrationNumber(registrationNumber: String = RegistrationNumberValid): Cookie =
    createCookie(DisposeFormRegistrationNumberCacheKey, registrationNumber)

  private val defaultDisposeTimestamp =
    new DateTime(DateOfDisposalYearValid.toInt,
      DateOfDisposalMonthValid.toInt,
      DateOfDisposalDayValid.toInt,
      0,
      0
    ).toString()
  def disposeFormTimestamp(timestamp: String = defaultDisposeTimestamp): Cookie =
    createCookie(DisposeFormTimestampIdCacheKey, timestamp)

  def disposeTransactionId(transactionId: String = TransactionIdValid): Cookie =
    createCookie(DisposeFormTransactionIdCacheKey, transactionId)

  def vehicleRegistrationNumber(registrationNumber: String = RegistrationNumberValid): Cookie =
    createCookie(DisposeFormRegistrationNumberCacheKey, registrationNumber)

  def preventGoingToDisposePage(payload: String = ""): Cookie =
    createCookie(PreventGoingToDisposePageCacheKey, payload)

  def disposeOccurred = createCookie(DisposeOccurredCacheKey, "")

  def help(origin: String = HelpPage.address): Cookie = {
    val key = HelpCacheKey
    val value = origin
    createCookie(key, value)
  }

  def disposeSurveyUrl(surveyUrl: String): Cookie =
    createCookie(SurveyRequestTriggerDateCacheKey, surveyUrl)

  def microServiceError(origin: String = VehicleLookupPage.address): Cookie = {
    val key = MicroServiceErrorRefererCacheKey
    val value = origin
    createCookie(key, value)
  }
}
