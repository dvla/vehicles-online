package helpers.disposal_of_vehicle

import composition.TestComposition
import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import models.BusinessChooseYourAddressFormModel
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModel
import models.DisposeFormModel.DisposeFormModelCacheKey
import models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
import models.DisposeFormModel.DisposeFormTimestampIdCacheKey
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import models.DisposeFormModel.DisposeOccurredCacheKey
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import models.DisposeFormModel.SurveyRequestTriggerDateCacheKey
import models.EnterAddressManuallyFormModel
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.SeenCookieMessageCacheKey
import models.VehicleLookupFormModel
import models.VehicleLookupFormModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import org.joda.time.{LocalDate, DateTime}
import pages.disposal_of_vehicle.VehicleLookupPage
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSession
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import common.model.AddressModel
import common.model.BruteForcePreventionModel
import common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import common.model.SetupTradeDetailsFormModel
import common.model.SetupTradeDetailsFormModel.setupTradeDetailsCacheKey
import common.model.TraderDetailsModel
import common.model.TraderDetailsModel.traderDetailsCacheKey
import common.model.VehicleAndKeeperDetailsModel
import common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid
import webserviceclients.fakes.FakeAddressLookupService.TraderBusinessNameValid
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.TransactionIdValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.ReferenceNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleModelValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleMakeValid
import webserviceclients.fakes.{FakeDateServiceImpl, FakeDisposeWebServiceImpl}

object CookieFactoryForUnitSpecs extends TestComposition {

  // TODO can we make this more fluent by returning "this" at the end of the defs

  implicit private val cookieFlags = injector.getInstance(classOf[CookieFlags])
  final val TrackingIdValue = TrackingId("trackingId")
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
    val key = setupTradeDetailsCacheKey
    val value = SetupTradeDetailsFormModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode, traderEmail = None)
    createCookie(key, value)
  }

  def businessChooseYourAddressUseUprn(uprnSelected: String = traderUprnValid.toString): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def enterAddressManually(): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyFormModel(
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
    val key = traderDetailsCacheKey
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
    val key = traderDetailsCacheKey
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
    val key = traderDetailsCacheKey
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
    val key = traderDetailsCacheKey
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
    val key = bruteForcePreventionViewModelCacheKey
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
    val value = VehicleLookupFormModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber
    )
    createCookie(key, value)
  }

  def vehicleAndKeeperDetailsModel(registrationNumber: String = RegistrationNumberValid,
                                   vehicleMake: Option[String] = Some(VehicleMakeValid),
                                   vehicleModel: Option[String] = Some(VehicleModelValid),
                                   title: Option[String] = None,
                                   firstName: Option[String] = None,
                                   lastName: Option[String] = None,
                                   address: Option[AddressModel] = None,
                                   disposeFlag: Boolean = false,
                                   suppressedV5CFlag: Boolean = false): Cookie = {
    val key = vehicleAndKeeperLookupDetailsCacheKey
    val value = VehicleAndKeeperDetailsModel(
      registrationNumber = registrationNumber,
      make = vehicleMake,
      model = vehicleModel,
      title = title,
      firstName = firstName,
      lastName = lastName,
      address = address,
      disposeFlag = Some(disposeFlag),
      keeperEndDate = if (disposeFlag) Some(new DateTime()) else None,
      keeperChangeDate = None,
      suppressedV5Flag = Some(suppressedV5CFlag)
    )
    createCookie(key, value)
  }


  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure"): Cookie =
    createCookie(VehicleLookupResponseCodeCacheKey, responseCode)

  def disposeFormModel(mileage: Option[Int] = None): Cookie = {
    val key = DisposeFormModelCacheKey
    val value = DisposeFormModel(
      mileage = mileage,
      dateOfDisposal = new LocalDate(
        FakeDateServiceImpl.DateOfDisposalYearValid.toInt,
          FakeDateServiceImpl.DateOfDisposalMonthValid.toInt,
          FakeDateServiceImpl.DateOfDisposalDayValid.toInt
      ),
      consent = FakeDisposeWebServiceImpl.ConsentValid,
      lossOfRegistrationConsent = FakeDisposeWebServiceImpl.ConsentValid
    )
    createCookie(key, value)
  }

  def trackingIdModel(trackingId: TrackingId = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, trackingId.value)
  }

  def disposeFormRegistrationNumber(registrationNumber: String = RegistrationNumberValid): Cookie =
    createCookie(DisposeFormRegistrationNumberCacheKey, registrationNumber)

  private val defaultDisposeTimestamp =
    new DateTime(DateOfDisposalYearValid.toInt,
      DateOfDisposalMonthValid.toInt,
      DateOfDisposalDayValid.toInt,
      0,
      0
    ).toString

  def disposeFormTimestamp(timestamp: String = defaultDisposeTimestamp): Cookie =
    createCookie(DisposeFormTimestampIdCacheKey, timestamp)

  def disposeTransactionId(transactionId: TrackingId = TransactionIdValid): Cookie =
    createCookie(DisposeFormTransactionIdCacheKey, transactionId.value)

  def vehicleRegistrationNumber(registrationNumber: String = RegistrationNumberValid): Cookie =
    createCookie(DisposeFormRegistrationNumberCacheKey, registrationNumber)

  def preventGoingToDisposePage(payload: String = ""): Cookie =
    createCookie(PreventGoingToDisposePageCacheKey, payload)

  def disposeOccurred = createCookie(DisposeOccurredCacheKey, "")

  def disposeSurveyUrl(surveyUrl: String): Cookie =
    createCookie(SurveyRequestTriggerDateCacheKey, surveyUrl)

  def microServiceError(origin: String = VehicleLookupPage.address): Cookie = {
    val key = MicroServiceErrorRefererCacheKey
    val value = origin
    createCookie(key, value)
  }
}
