package controllers

import Common.PrototypeHtml
import helpers.JsonUtils.deserializeJsonToModel
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.EnterAddressManuallyFormModel
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.EnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import org.mockito.Mockito.when
import pages.disposal_of_vehicle.{SetupTradeDetailsPage, VehicleLookupPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, contentAsString, defaultAwaitTimeout}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.traderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.AddressLinesId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.BuildingNameOrNumberId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.Line2Id
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.Line3Id
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.PostTownId
import utils.helpers.Config
import views.disposal_of_vehicle.EnterAddressManually.PostcodeId
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid

class EnterAddressManuallyUnitSpec extends UnitSpec {
  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to SetupTraderDetails page when present with no dealer name cached" in new WithApplication {
      val request = FakeRequest()
      val result = enterAddressManually.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = enterAddressManually.present(request)
      val content = contentAsString(result)
      content should include(filledValue(BuildingNameOrNumberValid))
      content should include(filledValue(Line2Valid))
      content should include(filledValue(Line3Valid))
      content should include(filledValue(PostTownValid))
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val content = contentAsString(present)
      content should not include filledValue(BuildingNameOrNumberValid)
      content should not include filledValue(Line2Valid)
      content should not include filledValue(Line3Valid)
      content should not include filledValue(PostTownValid)
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val enterAddressManuallyPrototypeNotVisible = new EnterAddressManually()

      val result = enterAddressManuallyPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "return bad request when no data is entered" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())

      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return bad request a valid postcode is entered without an address" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to Dispose after a valid submission of all fields" in new WithApplication {
      val request = requestWithValidDefaults()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
        val cookies = fetchCookiesFromHeaders(r)
        val enterAddressManuallyCookieName = EnterAddressManuallyCacheKey
        cookies.find(_.name == enterAddressManuallyCookieName) match {
          case Some(cookie) =>
            val json = cookie.value
            val model = deserializeJsonToModel[EnterAddressManuallyFormModel](json)

            model.addressAndPostcodeModel.addressLinesModel.buildingNameOrNumber should equal(
              BuildingNameOrNumberValid.toUpperCase)
            model.addressAndPostcodeModel.addressLinesModel.line2 should equal(Some(Line2Valid.toUpperCase))
            model.addressAndPostcodeModel.addressLinesModel.line3 should equal(Some(Line3Valid.toUpperCase))
            model.addressAndPostcodeModel.addressLinesModel.postTown should equal(PostTownValid.toUpperCase)
          case None => fail(s"$enterAddressManuallyCookieName cookie not found")
        }

        cookies.find(_.name == traderDetailsCookieName) match {
          case Some(cookie) =>
            val json = cookie.value
            val model = deserializeJsonToModel[TraderDetailsModel](json)
            val expectedData = Seq(BuildingNameOrNumberValid.toUpperCase,
              Line2Valid.toUpperCase,
              Line3Valid.toUpperCase,
              PostTownValid.toUpperCase,
              PostcodeValid.toUpperCase)
            expectedData should equal(model.traderAddress.address)

          case None => fail(s"$traderDetailsCookieName cookie not found")
        }
      }
    }

    "redirect to Dispose after a valid submission of mandatory fields" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "submit removes commas and full stops from the end of each address line" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house,",
        line2 = "my street.",
        line3 = "my area.",
        postTown = "my town,"
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE,",
        line2 = "MY STREET.",
        line3 = "MY AREA.",
        postTown = "MY TOWN,"
      )
    }

    "submit removes multiple commas and full stops from the end of each address line" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house,.,..,,",
        line2 = "my street...,,.,",
        line3 = "my area.,,..",
        postTown = "my town,,,.,,,."
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE,.,..,,",
        line2 = "MY STREET...,,.,",
        line3 = "MY AREA.,,..",
        postTown = "MY TOWN,,,.,,,."
      )
    }

    "submit does not remove multiple commas and full stops from the middle of address lines" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house 1.1",
        line2 = "st. something street",
        line3 = "st. johns",
        postTown = "my t.own"
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE 1.1",
        line2 = "ST. SOMETHING STREET",
        line3 = "ST. JOHNS",
        postTown = "MY T.OWN"
      )
    }

    "submit removes commas, but still applies the min length rule" in new WithApplication {
      FormExtensions.trimNonWhiteListedChars("""[A-Za-z0-9\-]""")(",, m...,,,,   ") should equal("m")
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "m      "  // This should be a min length of 4 chars
      ))
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "submit does not accept an address containing only full stops" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "...")
      )

      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to SetupTraderDetails page when valid submit with no dealer name cached" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetupTradeDetails page when bad submit with no dealer name cached" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "write cookie after a valid submission of all fields" in new WithApplication {
      val request = requestWithValidDefaults()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(traderDetailsCacheKey)
      }
    }

    "collapse error messages for buildingNameOrNumber" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> "",
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Building/number and street must contain between 4 and 30 characters")
    }

    "collapse error messages for post town" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> "",
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Town or city must contain between 3 and 20 characters")
    }
  }

  private lazy val enterAddressManually = {
    injector.getInstance(classOf[EnterAddressManually])
  }

  private val traderDetailsCookieName = CookiePrefix + "traderDetails"

  private def validateAddressCookieValues(result: Future[Result], buildingName: String, line2: String,
                                          line3: String, postTown: String, postCode: String = PostcodeValid) = {

    whenReady(result) { r =>
      val cookies = fetchCookiesFromHeaders(r)
      cookies.find(_.name == traderDetailsCookieName) match {
        case Some(cookie) =>
          val json = cookie.value
          val model = deserializeJsonToModel[TraderDetailsModel](json)
          val expectedData = Seq(buildingName,
            line2,
            line3,
            postTown,
            postCode)
          expectedData should equal(model.traderAddress.address)
        case None => fail(s"$traderDetailsCookieName cookie not found")
      }
    }
  }

  private def requestWithValidDefaults(buildingName: String = BuildingNameOrNumberValid,
                                      line2: String = Line2Valid,
                                      line3: String = Line3Valid,
                                      postTown: String = PostTownValid,
                                      postCode: String = PostcodeValid) =

    FakeRequest().withFormUrlEncodedBody(
      s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> buildingName,
      s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> line2,
      s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> line3,
      s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> postTown,
      s"$AddressAndPostcodeId.$PostcodeId" -> postCode).
      withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())

  private def filledValue(value: String) =
    s"""value="$value""""

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
    enterAddressManually.present(request)
  }
}
