package webserviceclients.dispose_service

import play.api.libs.json.Json
import helpers.{WithApplication, UnitSpec}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, NoCookieFlags}
import helpers.WireMockFixture
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import webserviceclients.dispose.{DisposeConfig, DisposalAddressDto, DisposeRequestDto, DisposeWebServiceImpl}
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}

class DisposeWebServiceImplSpec extends UnitSpec with WireMockFixture {

  implicit val noCookieFlags = new NoCookieFlags
  implicit lazy val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
  lazy val disposeService = new DisposeWebServiceImpl(new DisposeConfig() {
    override lazy val baseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = "track-id-test"

  implicit val disposalAddressDtoFormat = Json.format[DisposalAddressDto]
  implicit val disposeRequestFormat = Json.format[DisposeRequestDto]
  val request = DisposeRequestDto(
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
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }
}

