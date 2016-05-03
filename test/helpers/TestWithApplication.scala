package helpers

import composition.TestGlobalWithFilters
import helpers.TestWithApplication.fakeAppWithTestGlobal
import play.api.test.{FakeApplication, WithApplication}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

abstract class TestWithApplication(testApp: FakeApplication = fakeAppWithTestGlobal) extends WithApplication(testApp)

object TestWithApplication {
  private lazy val fakeAppWithTestGlobal: FakeApplication = LightFakeApplication(TestGlobalWithFilters)
}
