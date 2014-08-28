package helpers.disposal_of_vehicle

import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel, AddressModel, BruteForcePreventionModel}
import BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import org.joda.time.DateTime
import org.openqa.selenium.{Cookie, WebDriver}
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{Json, Writes}
import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.{CyId, EnId}
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel, DayMonthYear}
import viewmodels.BusinessChooseYourAddressViewModel.BusinessChooseYourAddressCacheKey
import viewmodels.DisposeFormViewModel.{DisposeFormModelCacheKey, DisposeFormRegistrationNumberCacheKey, DisposeFormTimestampIdCacheKey, DisposeFormTransactionIdCacheKey, DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey}
import viewmodels.EnterAddressManuallyViewModel.EnterAddressManuallyCacheKey
import viewmodels.SetupTradeDetailsViewModel.SetupTradeDetailsCacheKey
import TraderDetailsModel.TraderDetailsCacheKey
import VehicleDetailsModel.VehicleLookupDetailsCacheKey
import viewmodels.VehicleLookupFormViewModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import viewmodels._
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, PostcodeValid, TraderBusinessNameValid, addressWithoutUprn}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import webserviceclients.fakes.FakeDisposeWebServiceImpl.TransactionIdValid
import webserviceclients.fakes.FakeVehicleLookupWebService.{KeeperNameValid, ReferenceNumberValid, RegistrationNumberValid, VehicleModelValid}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import webserviceclients.fakes.{FakeDisposeWebServiceImpl, FakeVehicleLookupWebService}

object CookieFactoryForUISpecs {
  private def addCookie[A](key: String, value: A)(implicit tjs: Writes[A], webDriver: WebDriver): Unit = {
    val valueAsString = Json.toJson(value).toString()
    val manage = webDriver.manage()
    val cookie = new Cookie(key, valueAsString)
    manage.addCookie(cookie)
  }

  def withLanguageCy()(implicit webDriver: WebDriver) = {
    val key = Play.langCookieName
    val value = CyId
    addCookie(key, value)
    this
  }

  def withLanguageEn()(implicit webDriver: WebDriver) = {
    val key = Play.langCookieName
    val value = EnId
    addCookie(key, value)
    this
  }

  def setupTradeDetails(traderPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupTradeDetailsCacheKey
    val value = SetupTradeDetailsViewModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode)
    addCookie(key, value)
    this
  }

  def businessChooseYourAddress(uprn: Long = traderUprnValid)(implicit webDriver: WebDriver) = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressViewModel(uprnSelected = uprn.toString)
    addCookie(key, value)
    this
  }

  def enterAddressManually()(implicit webDriver: WebDriver) = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyViewModel(addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(buildingNameOrNumber = BuildingNameOrNumberValid,
      line2 = Some(Line2Valid),
      line3 = Some(Line3Valid),
      postTown = PostTownValid)))
    addCookie(key, value)
    this
  }

  def dealerDetails(address: AddressModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(traderName = TraderBusinessNameValid, traderAddress = address)
    addCookie(key, value)
    this
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString)
                                   (implicit webDriver: WebDriver) = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology
    )
    addCookie(key, value)
    this
  }

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid)
                            (implicit webDriver: WebDriver) = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormViewModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber)
    addCookie(key, value)
    this
  }

  def vehicleDetailsModel(registrationNumber: String = RegistrationNumberValid,
                          vehicleMake: String = FakeVehicleLookupWebService.VehicleMakeValid,
                          vehicleModel: String = VehicleModelValid,
                          keeperName: String = KeeperNameValid)
                         (implicit webDriver: WebDriver) = {
    val key = VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(registrationNumber = registrationNumber,
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel,
      disposeFlag = true)
    addCookie(key, value)
    this
  }

  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure")
                               (implicit webDriver: WebDriver) = {
    val key = VehicleLookupResponseCodeCacheKey
    val value = responseCode
    addCookie(key, value)
    this
  }

  def disposeFormModel()(implicit webDriver: WebDriver) = {
    val key = DisposeFormModelCacheKey
    val value = DisposeFormViewModel(mileage = None,
      dateOfDisposal = DayMonthYear.today,
      consent = FakeDisposeWebServiceImpl.ConsentValid,
      lossOfRegistrationConsent = FakeDisposeWebServiceImpl.ConsentValid)
    addCookie(key, value)
    this
  }

  def disposeTransactionId(transactionId: String = TransactionIdValid)(implicit webDriver: WebDriver) = {
    val key = DisposeFormTransactionIdCacheKey
    val value = transactionId
    addCookie(key, value)
    this
  }

  def disposeFormTimestamp()(implicit webDriver: WebDriver) = {
    val key = DisposeFormTimestampIdCacheKey
    val value = new DateTime(DateOfDisposalYearValid.toInt,
      DateOfDisposalMonthValid.toInt,
      DateOfDisposalDayValid.toInt,
      0,
      0
    ).toString()
    addCookie(key, value)
    this
  }

  def vehicleRegistrationNumber()(implicit webDriver: WebDriver) = {
    val key = DisposeFormRegistrationNumberCacheKey
    val value = RegistrationNumberValid
    addCookie(key, value)
    this
  }

  def preventGoingToDisposePage(url: String)(implicit webDriver: WebDriver) = {
    val key = PreventGoingToDisposePageCacheKey
    val value = url
    addCookie(key, value)
    this
  }

  def disposeOccurred(implicit webDriver: WebDriver) = {
    val key = DisposeOccurredCacheKey
    addCookie(key, "")
    this
  }

  def microServiceError(origin: String)(implicit webDriver: WebDriver) = {
    val key = MicroServiceErrorRefererCacheKey
    val value = origin
    addCookie(key, value)
    this
  }
}
