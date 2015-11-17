package webserviceclients.dispose_service

import play.api.libs.json.Json
import helpers.{WithApplication, UnitSpec}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.NoCookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import helpers.WireMockFixture
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeWebServiceImpl}
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}

class DisposeWebServiceImplSpec extends UnitSpec with WireMockFixture {

  implicit val noCookieFlags = new NoCookieFlags
  implicit lazy val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
  lazy val disposeService = new DisposeWebServiceImpl(new FakeDisposeConfig() {
    override lazy val baseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = TrackingId("track-id-test")

  implicit val disposalAddressDtoFormat = Json.format[DisposalAddressDto]
  implicit val disposeRequestFormat = Json.format[DisposeRequestDto]
  val request = DisposeRequestDto(
    VssWebHeaderDto(
      "trackingId",
      new org.joda.time.DateTime("2014-03-04T00:00:00.000Z"),
      "WEBDTT",
      "WEBDTT",
      VssWebEndUserDto("DTT", "DTT")
    ),
    referenceNumber = "ref number",
    registrationNumber = "reg number",
    traderName = "trader test",
    traderAddress = DisposalAddressDto(
      line = Seq("line1", "line2"),
      postTown = Some("town"),
      postCode = "W193NE",
      uprn = Some(3123L)
    ),
    dateOfDisposal = "",
    transactionTimestamp = "",
    prConsent = true,
    keeperConsent = false,
    mileage = Some(12)
  )

  "callDisposeService" should {
    "send the serialised json request" in new WithApplication {
      val resultFuture = disposeService.callDisposeService(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicles/dispose/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId.value)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }
}

