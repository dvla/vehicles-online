package webserviceclients.fakes

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

object FakeAddressLookupService {
  final val TraderBusinessNameValid = "example trader name"
  final val PostcodeWithoutAddresses = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  final val addressWithoutUprn = AddressModel(address = Seq("44 Hythe Road", "White City", "London", PostcodeValid))
  final val BuildingNameOrNumberValid = "12345ABCD"
  final val Line2Valid = "line2 stub"
  final val Line3Valid = "line3 stub"
  final val PostTownValid = "postTown stub"

  final val PostcodeValidWithSpace = "QQ9 9QQ"
}
