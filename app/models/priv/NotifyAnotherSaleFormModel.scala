package models.priv

import play.api.data.Forms.{mapping, nonEmptyText}

final case class NotifyAnotherSaleFormModel(selection: String)

object NotifyAnotherSaleFormModel {
  object Form {
    final val NotifyAnotherSaleId = "notifyAnotherSale"
    final val Mapping = mapping(
      NotifyAnotherSaleId -> nonEmptyText
    )(NotifyAnotherSaleFormModel.apply)(NotifyAnotherSaleFormModel.unapply)
  }
}
