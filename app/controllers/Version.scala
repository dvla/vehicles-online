package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.{VehicleLookupConfig, OrdnanceSurveyConfig}
import webserviceclients.dispose.DisposeConfig

class Version @Inject()(vehicleLookupConfig: VehicleLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        disposeConfig: DisposeConfig)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehicleLookupConfig.baseUrl + "/version",
    disposeConfig.baseUrl + "/version"
  )
