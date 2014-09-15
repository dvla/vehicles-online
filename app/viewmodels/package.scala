import controllers.MicroServiceError
import MicroServiceError.MicroServiceErrorRefererCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel, BruteForcePreventionModel}
import BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import viewmodels.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import viewmodels.DisposeFormModel.DisposeFormModelCacheKey
import viewmodels.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
import viewmodels.DisposeFormModel.DisposeFormTimestampIdCacheKey
import viewmodels.DisposeFormModel.DisposeFormTransactionIdCacheKey
import viewmodels.DisposeFormModel.DisposeOccurredCacheKey
import viewmodels.DisposeFormModel.PreventGoingToDisposePageCacheKey
import viewmodels.EnterAddressManuallyViewModel.EnterAddressManuallyCacheKey
import viewmodels.SetupTradeDetailsViewModel.SetupTradeDetailsCacheKey
import TraderDetailsModel.TraderDetailsCacheKey
import VehicleDetailsModel.VehicleLookupDetailsCacheKey
import viewmodels.VehicleLookupFormViewModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}

package object viewmodels {
  final val HelpCacheKey = "help"
  final val SeenCookieMessageCacheKey = "seen_cookie_message"

  // TODO: what is this set of cookies for?
  final val DisposeOnlyCacheKeys = Set(
    DisposeFormModelCacheKey,
    DisposeFormTransactionIdCacheKey,
    DisposeFormTimestampIdCacheKey,
    DisposeFormRegistrationNumberCacheKey
  )

  // Set of cookies related to a single vehicle disposal. Removed once the vehicle is successfully disposed
  final val DisposeCacheKeys = Set(
    BruteForcePreventionViewModelCacheKey,
    VehicleLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey,
    VehicleLookupFormModelCacheKey,
    DisposeFormModelCacheKey,
    DisposeFormTransactionIdCacheKey,
    DisposeFormTimestampIdCacheKey,
    DisposeFormRegistrationNumberCacheKey
  )

  // Set of cookies that store the trade details data. These are retained after a successful disposal
  // so the trader does not have to re-enter their details when disposing subsequent vehicles
  final val TradeDetailsCacheKeys = Set(SetupTradeDetailsCacheKey,
    TraderDetailsCacheKey,
    BusinessChooseYourAddressCacheKey,
    EnterAddressManuallyCacheKey)

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys = TradeDetailsCacheKeys.++(DisposeCacheKeys)
    .++(Set(PreventGoingToDisposePageCacheKey))
    .++(Set(DisposeOccurredCacheKey))
    .++(Set(HelpCacheKey))
    .++(Set(MicroServiceErrorRefererCacheKey))
}
