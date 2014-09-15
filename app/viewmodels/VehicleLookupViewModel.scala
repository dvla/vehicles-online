package viewmodels

import play.api.data.Form

case class VehicleLookupViewModel(form: Form[viewmodels.VehicleLookupFormModel],
                                  displayExitButton: Boolean, 
                                  surveyUrl: Option[String], 
                                  traderName: String, 
                                  address: Seq[String])
