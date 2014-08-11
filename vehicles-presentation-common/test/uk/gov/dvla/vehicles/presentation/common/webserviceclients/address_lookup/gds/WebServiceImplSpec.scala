package uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.gds

import uk.gov.dvla.vehicles.presentation.common.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.gds.WebServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.GDSAddressLookupConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeValidWithSpace}

final class WebServiceImplSpec extends UnitSpec {
  "postcodeWithNoSpaces" should {
    "return the same string if no spaces present" in {
      val result = addressLookupService.postcodeWithNoSpaces(PostcodeValid)

      result should equal(PostcodeValid)
    }

    "remove spaces when present" in {
      val result = addressLookupService.postcodeWithNoSpaces(PostcodeValidWithSpace)

      result should equal(PostcodeValid)
    }
  }

  private val addressLookupService = new WebServiceImpl(new GDSAddressLookupConfig)
}
