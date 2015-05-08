package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupConfig
import webserviceclients.dispose.DisposeConfig

class Version @Inject()(vehicleAndKeeperLookupConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        disposeConfig: DisposeConfig)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehicleAndKeeperLookupConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + "/version",
    disposeConfig.baseUrl + "/version"
  )
