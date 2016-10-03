package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Given, Then}
import java.security.cert.X509Certificate
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.ssl.SSLContexts
import org.openqa.selenium.WebDriver
import scala.io.Source.fromInputStream
import gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WebDriverFactory}

class VersionSteps(webBrowserDriver: WebBrowserDriver) extends AcceptanceTestHelper {
  private var versionString: String = null

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  @Given("^the user is on the version page$")
  def the_user_is_on_the_version_page() {
    val sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
      override def isTrusted(chain: Array[X509Certificate], authType: String): Boolean = true
    }).build()

    val httpClient = HttpClientBuilder.create.setSSLContext(sslContext).build()
    val post = new HttpGet(WebDriverFactory.testUrl +  "version")
    val httpResponse = httpClient.execute(post)
    versionString = fromInputStream(httpResponse.getEntity.getContent).mkString
    httpResponse.close()
  }

  @Then("^The user should be able to see version and runtime information for the webapp$")
  def the_user_should_be_able_to_see_version_and_runtime_information_for_the_webapp() = {
    versionString should include("Name")
    versionString should include("Version")
    versionString should include("Build on")
    versionString should include("Runtime OS")
  }

  @Then("^The user should be able to see version and runtime information for the microservices$")
  def the_user_should_be_able_to_see_version_and_runtime_information_for_the_microservices() {
    versionString should include("email-service")
    versionString should include("os-address-lookup")
    versionString should include("vehicle-and-keeper-lookup")
    versionString should include("vehicles-dispose-fulfil")
  }
}
