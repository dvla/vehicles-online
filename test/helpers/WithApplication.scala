package helpers

import composition.TestGlobal
import play.api.test.FakeApplication
import helpers.WithApplication.fakeAppWithTestGlobal

abstract class WithApplication(app: FakeApplication = fakeAppWithTestGlobal)
  extends play.api.test.WithApplication(app = app)

object WithApplication {
  private lazy val fakeAppWithTestGlobal: FakeApplication = FakeApplication(withGlobal = Some(TestGlobal))
}