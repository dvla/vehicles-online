package helpers

import composition.TestGlobal
import play.api.test.FakeApplication
import helpers.WithApplication.fakeAppWithTestGlobal
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

abstract class WithApplication(app: FakeApplication = fakeAppWithTestGlobal)
  extends play.api.test.WithApplication(app = app)

object WithApplication {
  private lazy val fakeAppWithTestGlobal: FakeApplication = LightFakeApplication(TestGlobal)
}