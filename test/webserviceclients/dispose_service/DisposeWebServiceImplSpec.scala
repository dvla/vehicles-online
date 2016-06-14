package webserviceclients.dispose_service

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import helpers.{UnitSpec, WireMockFixture, TestWithApplication}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.NoCookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeWebServiceImpl}

class DisposeWebServiceImplSpec extends UnitSpec with WireMockFixture {

  implicit val noCookieFlags = new NoCookieFlags
  implicit lazy val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
  lazy val disposeService = new DisposeWebServiceImpl(new FakeDisposeConfig() {
    override lazy val baseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = TrackingId("track-id-test")

  implicit val disposalAddressDtoFormat = Json.format[DisposalAddressDto]
  implicit val disposeRequestFormat = Json.format[DisposeRequestDto]
  private val saleDate = LocalDate.now.minusYears(2)

  val request = DisposeRequestDto(
    VssWebHeaderDto(
      "trackingId",
      saleDate.toDateTimeAtStartOfDay,
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
      postCode = "W193NE"
    ),
    dateOfDisposal = "",
    transactionTimestamp = "",
    prConsent = true,
    keeperConsent = false,
    mileage = Some(12)
  )

  "callDisposeService" should {
    "send the serialised json request" in new TestWithApplication {
      val resultFuture = disposeService.callDisposeService(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicles/dispose/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId.value))
          .withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }
}
