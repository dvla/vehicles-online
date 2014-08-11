package uk.gov.dvla.vehicles.presentation.common.webserviceclients.address_lookup.ordnance_survey

import org.scalatest.concurrent.PatienceConfiguration.Interval
import uk.gov.dvla.vehicles.presentation.common.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, NoCookieFlags, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.WireMockFixture
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.WebServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.OrdnanceSurveyConfig
import scala.concurrent.duration.DurationInt
import play.api.i18n.Lang
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeValidWithSpace}
import com.github.tomakehurst.wiremock.client.WireMock.{getRequestedFor, urlEqualTo, equalTo}

final class WebServiceImplSpec extends UnitSpec  with WireMockFixture {

  val trackingIdValue = "trackingIdValue"
  val interval = Interval(50.millis)

  implicit val noCookieFlags = new NoCookieFlags
  implicit val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
  val addressLookupService = new WebServiceImpl(new OrdnanceSurveyConfig() {
    override val baseUrl = s"http://localhost:$wireMockPort"
  })

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

  "callPostcodeWebService" should {
    "send the trackingId to the PostcodeWebService" in {
      val postCode = "N193NN"

      val futureResult = addressLookupService.callPostcodeWebService(postCode, trackingIdValue)(lang = Lang("en"))

      whenReady(futureResult, timeout, interval) { result =>
        wireMock.verifyThat(1, getRequestedFor(
          urlEqualTo(s"/postcode-to-address?postcode=$postCode&languageCode=EN&tracking_id=$trackingIdValue")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingIdValue)))
      }
    }

    "send the trackingId to the callUprnWebService" in {
      val postCode = "N193NN"

      val futureResult = addressLookupService.callUprnWebService(postCode, trackingIdValue)(Lang("en"))

      whenReady(futureResult, timeout, interval) { result =>
        wireMock.verifyThat(1, getRequestedFor(
          urlEqualTo(s"/uprn-to-address?uprn=$postCode&languageCode=EN&tracking_id=$trackingIdValue")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingIdValue)))
      }
    }
  }
}
