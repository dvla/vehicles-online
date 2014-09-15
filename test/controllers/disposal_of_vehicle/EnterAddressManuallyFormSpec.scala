package controllers.disposal_of_vehicle

import controllers.EnterAddressManually
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel
import AddressLinesViewModel.Form.LineMaxLength
import helpers.UnitSpec
import models.EnterAddressManuallyFormModel
import EnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import AddressLinesViewModel.Form.{AddressLinesId, BuildingNameOrNumberId, Line2Id, Line3Id, PostTownId}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}

final class EnterAddressManuallyFormSpec extends UnitSpec {
  "form" should {
    "accept if form is valid with all fields filled in" in {
      val model = formWithValidDefaults().get.addressAndPostcodeModel

      model.addressLinesModel.buildingNameOrNumber should equal(BuildingNameOrNumberValid.toUpperCase)
      model.addressLinesModel.line2 should equal(Some(Line2Valid.toUpperCase))
      model.addressLinesModel.line3 should equal(Some(Line3Valid.toUpperCase))
      model.addressLinesModel.postTown should equal(PostTownValid.toUpperCase)
    }

    "accept if form is valid with only mandatory filled in" in {
      val model = formWithValidDefaults(line2 = "", line3 = "").get.addressAndPostcodeModel

      model.addressLinesModel.buildingNameOrNumber should equal(BuildingNameOrNumberValid.toUpperCase)
    }
  }

  "address lines" should {
    "accept if form address lines contain hyphens" in {
      val model = formWithValidDefaults(buildingNameOrNumber = buildingNameOrNumberHypthens,
        line2 = line2Hypthens,line3 = line3Hypthens, postTown = postTownHypthens)
        .get.addressAndPostcodeModel

      model.addressLinesModel.buildingNameOrNumber should equal(buildingNameOrNumberHypthens.toUpperCase)
      model.addressLinesModel.line2 should equal(Some(line2Hypthens.toUpperCase))
      model.addressLinesModel.line3 should equal(Some(line3Hypthens.toUpperCase))
      model.addressLinesModel.postTown should equal(postTownHypthens.toUpperCase)
    }

    "reject when all fields are blank" in {
      formWithValidDefaults(buildingNameOrNumber = "", line2 = "", line3 = "", postTown = "").errors should have length 4
    }

    "reject if post town is blank" in {
      formWithValidDefaults(postTown = "").errors should have length 2
    }

    "reject if post town contains numbers" in {
      formWithValidDefaults(postTown = "123456").errors should have length 1
    }

    "accept if post town starts with spaces" in {
      formWithValidDefaults(postTown = " Swansea").get.addressAndPostcodeModel.addressLinesModel.
        postTown should equal("SWANSEA")
    }

    "reject if buildingNameOrNumber is blank" in {
      formWithValidDefaults(buildingNameOrNumber = "").errors should have length 2
    }

    "reject if buildingNameOrNumber is less than min length" in {
      formWithValidDefaults(buildingNameOrNumber = "abc", line2 = "", line3 = "", postTown = PostTownValid).
        errors should have length 1
    }

    "reject if buildingNameOrNumber is more than max length" in {
      formWithValidDefaults(buildingNameOrNumber = "a" * (LineMaxLength + 1),
        line2 = "", line3 = "", postTown = PostTownValid).errors should have length 1
    }

    "reject if buildingNameOrNumber is greater than max length" in {
      formWithValidDefaults(buildingNameOrNumber = "a" * (LineMaxLength + 1)).errors should have length 1
    }

    "reject if buildingNameOrNumber contains special characters" in {
      formWithValidDefaults(buildingNameOrNumber = "The*House").errors should have length 1
    }

    "reject if line2 is more than max length" in {
      formWithValidDefaults(line2 = "a" * (LineMaxLength + 1),
        line3 = "", postTown = PostTownValid).errors should have length 1
    }

    "reject if line3 is more than max length" in {
      formWithValidDefaults(line2 = "", line3 = "a" * (LineMaxLength + 1),
        postTown = PostTownValid).errors should have length 1
    }

    "reject if postTown is more than max length" in {
      formWithValidDefaults(line2 = "", line3 = "", postTown = "a" * (LineMaxLength + 1)).errors should have length 1
    }

    "reject if postTown is less than min length" in {
      formWithValidDefaults(line2 = "", line3 = "", postTown = "ab").errors should have length 1
    }

    "reject if total length of all address lines is more than maxLengthOfLinesConcatenated" in {
      formWithValidDefaults(
        buildingNameOrNumber = "a" * LineMaxLength + 1,
        line2 = "b" * LineMaxLength,
        line3 = "c" * LineMaxLength,
        postTown = "d" * LineMaxLength
      ).errors should have length 1
    }

    "reject if any line contains html chevrons" in {
      formWithValidDefaults(buildingNameOrNumber = "A<br>B").errors should have length 1
      formWithValidDefaults(line2 = "A<br>B").errors should have length 1
      formWithValidDefaults(line3 = "A<br>B").errors should have length 1
      formWithValidDefaults(postTown = "A<br>B").errors should have length 1
    }
  }

  private def formWithValidDefaults(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                    line2: String = Line2Valid,
                                    line3: String = Line3Valid,
                                    postTown: String = PostTownValid) = {
    injector.getInstance(classOf[EnterAddressManually]).form.bind(
      Map(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> buildingNameOrNumber,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> line2,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> line3,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> postTown
      )
    )
  }

  val buildingNameOrNumberHypthens = "1-12"
  val line2Hypthens = "address line - 2"
  val line3Hypthens = "address line - 3"
  val postTownHypthens = "address-line"
}
