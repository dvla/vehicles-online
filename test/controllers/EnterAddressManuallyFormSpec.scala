package controllers

import helpers.TestWithApplication
import helpers.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel.Form.PostcodeId
import models.EnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.AddressLinesId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.BuildingNameOrNumberId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.Line2Id
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.Line3Id
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.LineMaxLength
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.PostTownId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.TownMaxLength
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid

class EnterAddressManuallyFormSpec extends UnitSpec {

  "form" should {
    "accept if form is valid with all fields filled in" in new TestWithApplication {
      val model = formWithValidDefaults().get.addressAndPostcodeModel

      model.addressLinesModel.buildingNameOrNumber should equal(BuildingNameOrNumberValid.toUpperCase)
      model.addressLinesModel.line2 should equal(Some(Line2Valid.toUpperCase))
      model.addressLinesModel.line3 should equal(Some(Line3Valid.toUpperCase))
      model.addressLinesModel.postTown should equal(PostTownValid.toUpperCase)
    }

    "accept if form is valid with only mandatory filled in" in new TestWithApplication {
      val model = formWithValidDefaults(line2 = "", line3 = "").get.addressAndPostcodeModel

      model.addressLinesModel.buildingNameOrNumber should equal(BuildingNameOrNumberValid.toUpperCase)
    }
  }

  "address lines" should {
    "accept if form address lines contain hyphens" in new TestWithApplication {
      val model = formWithValidDefaults(buildingNameOrNumber = buildingNameOrNumberHypthens,
        line2 = line2Hypthens,line3 = line3Hypthens, postTown = postTownHypthens)
        .get.addressAndPostcodeModel

      model.addressLinesModel.buildingNameOrNumber should equal(buildingNameOrNumberHypthens.toUpperCase)
      model.addressLinesModel.line2 should equal(Some(line2Hypthens.toUpperCase))
      model.addressLinesModel.line3 should equal(Some(line3Hypthens.toUpperCase))
      model.addressLinesModel.postTown should equal(postTownHypthens.toUpperCase)
    }

    "reject when all fields are blank" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "",
        line2 = "",
        line3 = "",
        postTown = ""
      ).errors should have length 4
    }

    "reject if post town is blank" in new TestWithApplication {
      formWithValidDefaults(postTown = "").errors should have length 2
    }

    "reject if post town contains numbers" in new TestWithApplication {
      formWithValidDefaults(postTown = "123456").errors should have length 1
    }

    "accept if post town starts with spaces" in new TestWithApplication {
      formWithValidDefaults(postTown = " Swansea").get.addressAndPostcodeModel.addressLinesModel.
        postTown should equal("SWANSEA")
    }

    "reject if buildingNameOrNumber is blank" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "").errors should have length 2
    }

    "reject if buildingNameOrNumber is less than min length" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "abc", line2 = "", line3 = "", postTown = PostTownValid).
        errors should have length 1
    }

    "reject if buildingNameOrNumber is more than max length" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "a" * (LineMaxLength + 1),
        line2 = "", line3 = "", postTown = PostTownValid).errors should have length 1
    }

    "reject if buildingNameOrNumber is greater than max length" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "a" * (LineMaxLength + 1)).errors should have length 1
    }

    "reject if buildingNameOrNumber contains special characters" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "The*House").errors should have length 1
    }

    "reject if buildingNameOrNumber doesn't contain at least three alpha characters" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "1-12").errors should have length 1
    }

    "reject if line2 is more than max length" in new TestWithApplication {
      formWithValidDefaults(line2 = "a" * (LineMaxLength + 1),
        line3 = "", postTown = PostTownValid).errors should have length 1
    }

    "reject if line3 is more than max length" in new TestWithApplication {
      formWithValidDefaults(line2 = "", line3 = "a" * (LineMaxLength + 1),
        postTown = PostTownValid).errors should have length 1
    }

    "reject if postTown is more than max length" in new TestWithApplication {
      formWithValidDefaults(line2 = "", line3 = "", postTown = "a" * (LineMaxLength + 1)).errors should have length 1
    }

    "reject if postTown is less than min length" in new TestWithApplication {
      formWithValidDefaults(line2 = "", line3 = "", postTown = "ab").errors should have length 1
    }

    "reject if total length of all address lines is more than maxLengthOfLinesConcatenated" in new TestWithApplication {
      formWithValidDefaults(
        buildingNameOrNumber = "a" * LineMaxLength + 1,
        line2 = "b" * LineMaxLength,
        line3 = "c" * LineMaxLength,
        postTown = "d" * TownMaxLength
      ).errors should have length 1
    }

    "reject if any line contains html chevrons" in new TestWithApplication {
      formWithValidDefaults(buildingNameOrNumber = "A<br>B").errors should have length 1
      formWithValidDefaults(line2 = "A<br>B").errors should have length 1
      formWithValidDefaults(line3 = "A<br>B").errors should have length 1
      formWithValidDefaults(postTown = "A<br>B").errors should have length 1
    }

    "post code" should {
      "reject if post code is less than 5 characters" in new TestWithApplication {
        formWithValidDefaults(postCode = "SA1 1").errors.flatMap(_.messages) should contain theSameElementsAs
          List("error.restricted.validPostcode")
      }
      "reject if post code is greater than 5 characters" in new TestWithApplication {
        formWithValidDefaults(postCode = "SA1 1AAA").errors.flatMap(_.messages) should contain theSameElementsAs
          List("error.restricted.validPostcode")
      }
    }
  }

  private def formWithValidDefaults(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                    line2: String = Line2Valid,
                                    line3: String = Line3Valid,
                                    postTown: String = PostTownValid,
                                    postCode: String = PostcodeValid) = {
    injector.getInstance(classOf[EnterAddressManually]).form.bind(
      Map(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> buildingNameOrNumber,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> line2,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> line3,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> postTown,
        s"$AddressAndPostcodeId.$PostcodeId" -> postCode
      )
    )
  }

  val buildingNameOrNumberHypthens = "1-12ABC"
  val line2Hypthens = "address line - 2"
  val line3Hypthens = "address line - 3"
  val postTownHypthens = "address-line"
}
