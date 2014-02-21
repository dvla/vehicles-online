package modules

import com.tzavellas.sse.guice.ScalaModule
import services.{AddressLookupService, LoginWebService, V5cSearchWebService}
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logger
import ExecutionContext.Implicits.global
import models.domain.change_of_address.V5cSearchConfirmationModel
import models.domain.change_of_address.V5cSearchResponse
import models.domain.change_of_address.LoginPageModel
import models.domain.change_of_address.LoginResponse
import models.domain.change_of_address.LoginConfirmationModel
import models.domain.change_of_address.V5cSearchModel
import models.domain.disposal_of_vehicle.{AddressViewModel, AddressLinesModel, AddressAndPostcodeModel}

/**
 * Provides fake or test implementations for traits
 */
object TestModule extends ScalaModule {

  /**
   * Fake implementation of the WebService trait with in memory data
   */
  case class FakeV5cSearchWebService() extends V5cSearchWebService {
    override def invoke(cmd: V5cSearchModel): Future[V5cSearchResponse] = Future {
      V5cSearchResponse(true, "All ok ", V5cSearchConfirmationModel("vrn", "make", "model", "firstRegistered", "acquired"))
    }
  }

  /**
   * Fake implementation of the LoginWebService trait with in memory data
   */
  case class FakeLoginWebService() extends LoginWebService {
    val address1 = AddressViewModel(address = Seq("44 Hythe Road", "White City", "London", "NW10 6RJ"))

    override def invoke(cmd: LoginPageModel): Future[LoginResponse] = Future {
      LoginResponse(true, "All ok ", LoginConfirmationModel("Roger", "Booth", "21/05/1977", address1))
    }
  }

  /**
   * Fake implementation of the FakeAddressLookupService trait
   */
  class FakeAddressLookupService() extends AddressLookupService {
    val address1 = AddressViewModel(uprn = Some(1234), address = Seq("44 Hythe Road", "White City", "London", "NW10 6RJ"))
    val address2 = AddressViewModel(uprn = Some(4567), address = Seq("Penarth Road", "Cardiff", "CF11 8TT"))

    override def fetchAddressesForPostcode(postcode: String): Map[String, String] = {
      Map(
        address1.uprn.getOrElse(1234).toString -> address1.address.mkString(", "),
        address2.uprn.getOrElse(4567).toString -> address2.address.mkString(", ")
      ) // TODO this should come from call to GDS lookup.
    }

    override def lookupAddress(uprn: String): AddressViewModel = {
      val addresses = Map(
        address1.uprn.getOrElse(1234).toString -> address1,
        address2.uprn.getOrElse(4567).toString -> address2
      ) // TODO this should come from call to GDS lookup.

      addresses(uprn)
    }
  }

  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

    bind[V5cSearchWebService].to[FakeV5cSearchWebService]
    bind[LoginWebService].to[FakeLoginWebService]
    bind[AddressLookupService].to[FakeAddressLookupService]
  }
}