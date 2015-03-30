package controllers.priv

import javax.inject.Inject

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService
import utils.helpers.Config

class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends controllers.BusinessChooseYourAddress(addressLookupService) {

  protected override val submitCall = routes.BusinessChooseYourAddress.submit
  protected override val manualAddressEntryCall = routes.EnterAddressManually.present
  protected override val backCall = routes.SetUpTradeDetails.present
  protected override val redirectBack = Redirect(routes.SetUpTradeDetails.present)
  protected override val uprnNotFoundResult = Redirect(routes.UprnNotFound.present())
  protected override val successResult = Redirect(routes.VehicleLookup.present())
}
