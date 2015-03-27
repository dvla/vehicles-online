package controllers.priv

import javax.inject.Inject

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService
import utils.helpers.Config

class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends controllers.BusinessChooseYourAddress(addressLookupService) {

  protected override val submitCall = controllers.routes.BusinessChooseYourAddress.submit
  protected override val manualAddressEntryCall = controllers.routes.EnterAddressManually.present
  protected override val backCall = controllers.routes.SetUpTradeDetails.present
  protected override val redirectBack = Redirect(backCall)
  protected override val uprnNotFoundResult = Redirect(controllers.routes.UprnNotFound.present())
  protected override val successResult = Redirect(controllers.routes.VehicleLookup.present())
}
