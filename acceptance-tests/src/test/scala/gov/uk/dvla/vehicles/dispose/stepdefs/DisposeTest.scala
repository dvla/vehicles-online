package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.junit.Cucumber
import cucumber.api.junit.Cucumber.Options
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@Options(
  features = Array("acceptance-tests/src/test/resources/"),
  glue = Array("gov.uk.dvla.vehicles.dispose.stepdefs"),
  tags = Array("@tag")
)
class DisposeTest {

}
