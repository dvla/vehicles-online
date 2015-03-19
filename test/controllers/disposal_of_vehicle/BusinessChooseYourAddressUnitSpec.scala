package controllers.disposal_of_vehicle

import controllers.BusinessChooseYourAddress
import controllers.disposal_of_vehicle.Common.PrototypeHtml
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.DisposeCacheKeyPrefix.CookiePrefix
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import pages.disposal_of_vehicle.{SetupTradeDetailsPage, UprnNotFoundPage, VehicleLookupPage}
import play.api.mvc.Cookies
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, SET_COOKIE, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.traderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.fakes.FakeAddressLookupService.TraderBusinessNameValid
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.{responseValidForPostcodeToAddress, responseValidForPostcodeToAddressNotFound, responseValidForUprnToAddress, responseValidForUprnToAddressNotFound, traderUprnValid}

import scala.concurrent.Future

final class BusinessChooseYourAddressUnitSpec extends UnitSpec {

  "present (use UPRN enabled)" should {
    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = true), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddressUseUprn())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = true).present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include( s"""<option value="$traderUprnValid" selected>""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = true))
      content should include(TraderBusinessNameValid)
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = true).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = true)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(isPrototypeBannerVisible = false, ordnanceSurveyUseUprn = true).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "present (use UPRN not enabled for Northern Ireland)" should {
    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = false), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = false).present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include( s"""<option value="0" selected>""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = false))
      content should include(TraderBusinessNameValid)
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = false).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = false)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(isPrototypeBannerVisible = false, ordnanceSurveyUseUprn = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit (use UPRN enabled)" should {
    "redirect to VehicleLookup page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "return a bad request if no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$traderUprnValid" >""")
    }

    "redirect to setupTradeDetails page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setupTradeDetails page when bad submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = false, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(BusinessChooseYourAddressCacheKey, traderDetailsCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = false, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, traderDetailsCacheKey)
      }
    }
  }

  "submit (use UPRN not enabled for Northern Ireland)" should {
    "redirect to VehicleLookup page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "0").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "return a bad request if no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "redirect to setupTradeDetails page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "0")
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setupTradeDetails page when bad submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "0").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = false, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "0").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.filter( c => c.name == traderDetailsCacheKey).head.toString should include (""""property stub"""")
        cookies.map(_.name) should contain allOf(BusinessChooseYourAddressCacheKey, traderDetailsCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "0").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddress(uprnFound = false, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, traderDetailsCacheKey)
      }
    }
  }

  private def businessChooseYourAddress(uprnFound: Boolean = true,
                                        isPrototypeBannerVisible: Boolean = true,
                                        ordnanceSurveyUseUprn: Boolean) = {
    val responsePostcode = if (uprnFound) responseValidForPostcodeToAddress else responseValidForPostcodeToAddressNotFound
    val responseUprn = if (uprnFound) responseValidForUprnToAddress else responseValidForUprnToAddressNotFound
    val fakeWebService = new FakeAddressLookupWebServiceImpl(responsePostcode, responseUprn)
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val addressLookupService = new AddressLookupServiceImpl(fakeWebService, new DateServiceImpl, healthStatsMock)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.
    new BusinessChooseYourAddress(addressLookupService)
  }

  private def buildCorrectlyPopulatedRequest(addressSelected: String = traderUprnValid.toString) = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> addressSelected)
  }

  private def present(ordnanceSurveyUseUprn: Boolean) = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
    businessChooseYourAddress(uprnFound = true, ordnanceSurveyUseUprn = ordnanceSurveyUseUprn).present(request)
  }
}
