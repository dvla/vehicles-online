package services.address_lookup.gds

import services.AddressLookupService
import models.domain.disposal_of_vehicle.AddressViewModel
import utils.helpers.Config
import play.api.Logger
import com.ning.http.client.Realm.AuthScheme
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.libs.ws.Response
import services.address_lookup.gds.domain.{PlacesAddress, Address}
import services.address_lookup.gds.domain.JsonFormats.addressFormat
import services.address_lookup.ordnance_survey.domain.OSAddressbaseSearchResponse

class AddressLookupServiceImpl @Inject()(ws: services.WebService) extends AddressLookupService {
  val username = s"${Config.ordnanceSurveyUsername}"
  val password = s"${Config.ordnanceSurveyPassword}"
  val baseUrl = s"${Config.ordnanceSurveyBaseUrl}"

  override protected def callPostcodeWebService(postcode: String): Future[Response] = ???

  override protected def callUprnWebService(uprn: String): Future[Response] = ???

  def extractFromJson(resp: Response): Seq[Address] = {
    //resp.json.as[Seq[Address]]

    //val response = resp.json.asOpt[Seq[Address]]
    //response.flatMap(_.results)
    Seq.empty
  }

  override def fetchAddressesForPostcode(postcode: String): Future[Seq[(String, String)]] = {
    def toUprnsAndAddresses(resp: Response): Seq[(String, String)] = {
      extractFromJson(resp) map {address => (address.presentation.uprn, address.presentation.toViewModel) }
    }

    callPostcodeWebService(postcode).map { resp =>
      Logger.debug(s"Http response code from Ordnance Survey postcode lookup service was: ${resp.status}")
      if (resp.status == play.api.http.Status.OK) toUprnsAndAddresses(resp)
      else Seq.empty // The service returned http code other than 200
    }.recover {
      case e: Throwable =>
        Logger.error(s"Ordnance Survey postcode lookup service error: $e")
        Seq.empty
    }
  }

  def postcodeWithNoSpaces(postcode: String): String = postcode.filter(_ != ' ')

  override def fetchAddressForUprn(uprn: String): Future[Option[AddressViewModel]] = ???
}
