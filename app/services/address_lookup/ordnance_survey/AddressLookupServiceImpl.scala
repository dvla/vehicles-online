package services.address_lookup.ordnance_survey

import models.domain.disposal_of_vehicle.{UprnToAddressResponse, PostcodeToAddressResponse, AddressViewModel}
import play.api.Logger
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.libs.ws.Response
import services.address_lookup.{AddressLookupWebService, AddressLookupService}
import common.ClientSideSession
import mappings.disposal_of_vehicle.Logging

final class AddressLookupServiceImpl @Inject()(ws: AddressLookupWebService) extends AddressLookupService {
  override def fetchAddressesForPostcode(postcode: String)
                                        (implicit session: ClientSideSession): Future[Seq[(String, String)]] = {
    def extractFromJson(resp: Response): Option[PostcodeToAddressResponse] = {
      resp.json.asOpt[PostcodeToAddressResponse]
    }

    def toDropDown(resp: Response): Seq[(String, String)] =
      extractFromJson(resp) match {
        case Some(results) =>
          results.addresses map {
            address => (address.uprn, address.address)
          } // Sort before translating to drop down format.
        case None =>
          // Handle no results
          val postcodeToLog = Logging.anonymize(postcode)
          Logger.debug(s"No results returned for postcode: $postcodeToLog")
          Seq.empty
      }

    ws.callPostcodeWebService(postcode)(session).map {
      resp =>
        Logger.debug(s"Http response code from Ordnance Survey postcode lookup service was: ${resp.status}")
        if (resp.status == play.api.http.Status.OK) toDropDown(resp)
        else Seq.empty // The service returned http code other than 200 OK
    }.recover {
      case e: Throwable =>
        Logger.error(s"Ordnance Survey postcode lookup service error: " + e.toString.take(45))
        Seq.empty
    }
  }

  override def fetchAddressForUprn(uprn: String)
                                  (implicit session: ClientSideSession): Future[Option[AddressViewModel]] = {
    // Extract result from response and return as a view model.
    def extractFromJson(resp: Response): Option[UprnToAddressResponse] = {
      resp.json.asOpt[UprnToAddressResponse]
    }

    def toViewModel(resp: Response) =
      extractFromJson(resp) match {
        case Some(deserialized) => deserialized.addressViewModel
        case None =>
          val uprnToLog = Logging.anonymize(uprn)
          Logger.error(s"Could not deserialize response of web service for submitted UPRN: $uprnToLog")
          None
      }

    ws.callUprnWebService(uprn).map {
      resp =>
        Logger.debug(s"Http response code from Ordnance Survey uprn lookup service was: ${resp.status}")
        if (resp.status == play.api.http.Status.OK) toViewModel(resp)
        else None
    }.recover {
      case e: Throwable =>
        Logger.error(s"Ordnance Survey postcode lookup service error: " + e.toString.take(45))
        None
    }
  }
}
