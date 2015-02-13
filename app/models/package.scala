import controllers.MicroServiceError
import MicroServiceError.MicroServiceErrorRefererCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel, BruteForcePreventionModel}
import BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.DisposeFormModel.DisposeFormModelCacheKey
import models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
import models.DisposeFormModel.DisposeFormTimestampIdCacheKey
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import models.DisposeFormModel.DisposeOccurredCacheKey
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.SetupTradeDetailsFormModel.setupTradeDetailsCacheKey
import models.VehicleLookupFormModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import TraderDetailsModel.TraderDetailsCacheKey
import VehicleAndKeeperDetailsModel.VehicleAndKeeperLookupDetailsCacheKey

import models.DisposeCacheKeyPrefix.CookiePrefix

package object models {
  final val HelpCacheKey = s"${CookiePrefix}help"
  final val SeenCookieMessageCacheKey = "seen_cookie_message" // Same value across all exemplars

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
    VehicleAndKeeperLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey,
    VehicleLookupFormModelCacheKey,
    DisposeFormModelCacheKey,
    DisposeFormTransactionIdCacheKey,
    DisposeFormTimestampIdCacheKey,
    DisposeFormRegistrationNumberCacheKey
  )

  // Set of cookies that store the trade details data. These are retained after a successful disposal
  // so the trader does not have to re-enter their details when disposing subsequent vehicles
  final val TradeDetailsCacheKeys = Set(setupTradeDetailsCacheKey,
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
