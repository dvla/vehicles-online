package models

import play.api.data.Form
import play.api.mvc.Call

case class VehicleLookupViewModel(form: Form[models.VehicleLookupFormModel],
                                  displayExitButton: Boolean,
                                  surveyUrl: Option[String],
                                  traderName: String,
                                  address: Seq[String],
                                  traderEmail: Option[String],
                                  submit: Call,
                                  back: Call,
                                  exit: Call,
                                  resetTraderDetails: Call
                                 )
