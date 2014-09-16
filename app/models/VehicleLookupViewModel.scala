package models

import play.api.data.Form

case class VehicleLookupViewModel(form: Form[models.VehicleLookupFormModel],
                                  displayExitButton: Boolean, 
                                  surveyUrl: Option[String], 
                                  traderName: String, 
                                  address: Seq[String])
