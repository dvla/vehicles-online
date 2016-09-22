package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.controllers.Version.Suffix
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupConfig
import webserviceclients.dispose.DisposeConfig

class Version @Inject()(//vehicleAndKeeperLookupConfig: VehicleAndKeeperLookupConfig,
//                        osAddressLookupConfig: OrdnanceSurveyConfig,
//                        vehiclesDisposeConfig: DisposeConfig,
//                        emailConfig: EmailServiceConfig
                       )
  extends controllers.Version(
//    emailConfig.emailServiceMicroServiceBaseUrl + Suffix,
//    osAddressLookupConfig.baseUrl + Suffix,
//    vehicleAndKeeperLookupConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + Suffix,
//    vehiclesDisposeConfig.baseUrl + Suffix
  )
