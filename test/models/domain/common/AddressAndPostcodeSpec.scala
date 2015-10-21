package models.domain.common

import helpers.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel

class AddressAndPostcodeSpec extends UnitSpec {
  "Address - model" should {
    "return expected toString value" in {
      val address = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = "abcd",
          line2 = Some("e"),
          line3 = Some("f"),
          postTown = "ghi"
        ),
        postCode = "J"
      )

      val result = address.toViewFormat.mkString(", ")

      result should equal("ABCD, E, F, GHI, J")
    }

    "return expected toString value with missings values" in {
      val address = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = "abcd",
          line2 = None,
          line3 = None,
          postTown = "efg"
        ),
        postCode = "H"
      )

      val result = address.toViewFormat.mkString(", ")

      result should equal("ABCD, EFG, H")
    }
  }
}
