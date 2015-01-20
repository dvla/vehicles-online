package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common
import common.controllers
import common.webserviceclients.config.{VehicleAndKeeperLookupConfig, OrdnanceSurveyConfig}
import webserviceclients.dispose.DisposeConfig

class Version @Inject()(vehicleAndKeeperLookupConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        disposeConfig: DisposeConfig)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehicleAndKeeperLookupConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + "/version",
    disposeConfig.baseUrl + "/version"
  )
