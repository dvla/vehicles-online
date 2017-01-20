package views.disposal_of_vehicle

import composition.TestHarness
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost, HttpPut, HttpRequestBase}
import org.apache.http.impl.client.HttpClients
import play.mvc.Http.Status
import pages.disposal_of_vehicle.HealthCheckPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiSpec

class HealthCheckIntegrationSpec extends UiSpec with TestHarness {
  "Accessing the /healthcheck url" should {
    "return 200 for GET" in new WebBrowserForSelenium {
      val httpResponse = execute(new HttpGet(HealthCheckPage.url))
      try httpResponse.getStatusLine.getStatusCode should be(Status.OK)
      finally httpResponse.close()
    }

    "return 403 for POST" in new WebBrowserForSelenium {
      val httpResponse = execute(new HttpPost(HealthCheckPage.url))
      try httpResponse.getStatusLine.getStatusCode should be (Status.FORBIDDEN)
      finally httpResponse.close()
    }

    "return 404 for PUT etc." in new WebBrowserForSelenium {
      val httpResponse = execute(new HttpPut(HealthCheckPage.url))
      try httpResponse.getStatusLine.getStatusCode should be(Status.NOT_FOUND)
      finally httpResponse.close()
    }
  }

  private def execute(method: HttpRequestBase): CloseableHttpResponse = {
    val httpClient = HttpClients.createDefault()
    httpClient.execute(method)
  }
}
