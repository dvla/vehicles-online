import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import uk.gov.dvla.vehicles.presentation.common
import common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import common.model.MicroserviceResponseModel.MsResponseCacheKey
import common.model.SetupTradeDetailsFormModel.setupTradeDetailsCacheKey
import common.model.TraderDetailsModel.traderDetailsCacheKey
import common.model.VehicleAndKeeperDetailsModel
import VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey

package object models {
  final val IdentifierCacheKey = s"${CookiePrefix}identifier"

  final val DisposeOnlyCacheKeys = Set(
    models.DisposeFormModel.DisposeFormModelCacheKey,
    models.DisposeFormModel.DisposeFormTransactionIdCacheKey,
    models.DisposeFormModel.DisposeFormTimestampIdCacheKey,
    models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
  )

  final val PrivateDisposeOnlyCacheKeys = Set(
    models.PrivateDisposeFormModel.PrivateDisposeFormModelCacheKey,
    models.PrivateDisposeFormModel.DisposeFormTransactionIdCacheKey,
    models.PrivateDisposeFormModel.DisposeFormTimestampIdCacheKey,
    models.PrivateDisposeFormModel.DisposeFormRegistrationNumberCacheKey
  )

  // Set of cookies related to a single vehicle disposal. Removed once the vehicle is successfully disposed
  final val VehicleCacheKeys = Set(
    bruteForcePreventionViewModelCacheKey,
    vehicleAndKeeperLookupDetailsCacheKey,
    MsResponseCacheKey,
    VehicleLookupFormModelCacheKey
  )

  final val DisposeCacheKeys = VehicleCacheKeys ++ DisposeOnlyCacheKeys

  final val PrivateDisposeCacheKeys = VehicleCacheKeys ++ PrivateDisposeOnlyCacheKeys

  // Set of cookies that store the trade details data. These are retained after a successful disposal
  // so the trader does not have to re-enter their details when disposing subsequent vehicles
  final val TradeDetailsCacheKeys = Set(setupTradeDetailsCacheKey,
    traderDetailsCacheKey,
    BusinessChooseYourAddressCacheKey,
    EnterAddressManuallyCacheKey)

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys = TradeDetailsCacheKeys.++(DisposeCacheKeys)
    .++(Set(models.DisposeFormModel.PreventGoingToDisposePageCacheKey))
    .++(Set(models.DisposeFormModel.DisposeOccurredCacheKey))
    .+(MicroServiceErrorRefererCacheKey)
    .+(IdentifierCacheKey)

  final val PrivateAllCacheKeys = TradeDetailsCacheKeys.++(PrivateDisposeCacheKeys)
    .++(Set(models.PrivateDisposeFormModel.PreventGoingToDisposePageCacheKey))
    .++(Set(models.PrivateDisposeFormModel.DisposeOccurredCacheKey))
    .+(MicroServiceErrorRefererCacheKey)
    .+(IdentifierCacheKey)
}
