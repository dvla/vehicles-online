package models.domain.common

import helpers.UnitSpec
import viewmodels.common.{AddressLinesViewModel, AddressAndPostcodeViewModel}

final class AddressAndPostcodeSpec extends UnitSpec {
  "Address - model" should {
    "return expected toString value" in {
      val address = AddressAndPostcodeViewModel(addressLinesModel = AddressLinesViewModel(buildingNameOrNumber = "abcd",
        line2 = Some("e"),
        line3 = Some("f"),
        postTown = "ghi"))

      val result = address.toViewFormat(postcode = "J").mkString(", ")

      result should equal("ABCD, E, F, GHI, J")
    }

    "return expected toString value with missings values" in {
      val address = AddressAndPostcodeViewModel(addressLinesModel = AddressLinesViewModel(buildingNameOrNumber = "abcd",
        line2 = None,
        line3 = None,
        postTown = "efg"))

      val result = address.toViewFormat(postcode = "H").mkString(", ")

      result should equal("ABCD, EFG, H")
    }
  }
}
