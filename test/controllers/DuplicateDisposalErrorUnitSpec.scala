package controllers

import controllers.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout, status}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import utils.helpers.Config

class DuplicateDisposalErrorUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      status(present) should equal(OK)
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
      when(config.assetsUrl).thenReturn(None) // Stub this config value.
      implicit val dateService = new DateService {
          override def today = DayMonthYear(24, 12, 2015)
          val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
          override def now = formatter.parseDateTime("24/12/2015").toInstant
          override def dateTimeISOChronology: String = new DateTime(
            24, 12, 2015, 0, 0).toString
        }
      val duplicateDisposalErrorPrototypeNotVisible = new DuplicateDisposalError()
      val result = duplicateDisposalErrorPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private lazy val present = {
    val duplicateDisposalError = injector.getInstance(classOf[DuplicateDisposalError])
    val newFakeRequest = FakeRequest()
    duplicateDisposalError.present(newFakeRequest)
  }
}