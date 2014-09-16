package models.domain.common

import helpers.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid

final class AddressLinesModelSpec extends UnitSpec {
  "toViewFormat" should {
    "return all lines when all lines are set to a value" in {
      AddressLinesViewModel(
        buildingNameOrNumber = BuildingNameOrNumberValid,
        line2 = Some(Line2Valid),
        line3 = Some(Line3Valid),
        postTown = PostTownValid
      ).toViewFormat should equal(
        Seq(
          BuildingNameOrNumberValid.toUpperCase,
          Line2Valid.toUpperCase,
          Line3Valid.toUpperCase,
          PostTownValid.toUpperCase
        )
      )
    }

    "remove unset fields so there are no gaps" in {
      AddressLinesViewModel(
        buildingNameOrNumber = BuildingNameOrNumberValid,
        postTown = PostTownValid
      ).toViewFormat should equal(Seq(BuildingNameOrNumberValid.toUpperCase, PostTownValid.toUpperCase))
    }
  }

  "totalCharacters" should {
    "return zero for empty fields" in {
      AddressLinesViewModel(buildingNameOrNumber = "", postTown = "").totalCharacters should equal(0)
    }

    "return expected length when only mandatory fields are filled" in {
      AddressLinesViewModel(buildingNameOrNumber = BuildingNameOrNumberValid, postTown = PostTownValid)
        .totalCharacters should equal(BuildingNameOrNumberValid.length + PostTownValid.length)
    }

    "return expected length when some fields are not filled" in {
      AddressLinesViewModel(
        buildingNameOrNumber = BuildingNameOrNumberValid,
        line2 = None,
        line3 = None,
        postTown = PostTownValid
      ).totalCharacters should equal(BuildingNameOrNumberValid.length + PostTownValid.length)
    }

    "return expected length when all fields are filled" in {
      AddressLinesViewModel(
        buildingNameOrNumber = BuildingNameOrNumberValid,
        line2 = Some(Line2Valid),
        line3 = Some(Line3Valid),
        postTown = PostTownValid).totalCharacters should equal(BuildingNameOrNumberValid.length +
                                                               Line2Valid.length +
                                                               Line3Valid.length +
                                                               PostTownValid.length
      )
    }
  }
}
