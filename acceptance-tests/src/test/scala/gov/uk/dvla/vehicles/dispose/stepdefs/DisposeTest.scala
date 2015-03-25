package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/"),
  glue = Array("gov.uk.dvla.vehicles.dispose.stepdefs"),
  tags = Array("@working")
)
class DisposeTest {
}
