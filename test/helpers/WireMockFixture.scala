package helpers

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.{Request, RequestListener, Response}
import com.github.tomakehurst.wiremock.WireMockServer
import java.net.ServerSocket
import org.scalatest.{BeforeAndAfterEach, Suite, SuiteMixin}
import scala.collection.mutable

trait WireMockFixture extends Suite with SuiteMixin with BeforeAndAfterEach {

  val wireMockPort: Int = {
    val serverSocket = new ServerSocket(0)
    try serverSocket.getLocalPort
    catch {
      case e: Exception => 51987
    }
    finally serverSocket.close()
  }

  var wireMock: WireMock = _

  override def beforeEach() {
    wireMock = new WireMock("localhost", wireMockPort)
    wireMockServer.start()
  }

  override def afterEach() {
    wireMock.resetMappings()
    wireMockServer.stop()
  }

  val wireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

  def addRequestUrlListener(): mutable.ArrayBuffer[String] = {
    var sentRequestsUrls: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty[String]

    wireMockServer.addMockServiceRequestListener(new RequestListener() {
      override def requestReceived(request: Request, response: Response): Unit = {
        sentRequestsUrls += request.getUrl
      }
    })
    sentRequestsUrls
  }

  def addRequestResponseListener(): mutable.ArrayBuffer[(Request, Response)] = {
    var sentRequestsResponses: mutable.ArrayBuffer[(Request, Response)] = mutable.ArrayBuffer.empty[(Request, Response)]

    wireMockServer.addMockServiceRequestListener(new RequestListener() {
      override def requestReceived(request: Request, response: Response): Unit = {
        sentRequestsResponses += ((request, response))
      }
    })
    sentRequestsResponses
  }
}