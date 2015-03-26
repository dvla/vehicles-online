package models

import controllers.routes
import play.api.data.Form
import play.api.mvc.Call

case class BusinessChooseYourAddressViewModel(businessChooseYourAddressForm: Form[models.BusinessChooseYourAddressFormModel],
                                              traderBusinessName: String,
                                              traderPostcode: String,
                                              dropDownOptions: Seq[(String, String)],
                                              submit: Call,
                                              manualAddressEntry: Call,
                                              back: Call = routes.SetUpTradeDetails.present)
