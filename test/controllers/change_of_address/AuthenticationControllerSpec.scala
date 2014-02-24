package controllers.change_of_address

import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers._
import org.scalatest.{Matchers, WordSpec}
import mappings.change_of_address
import change_of_address.Authentication._
import org.specs2.mock.Mockito
import helpers.change_of_address.{LoginCachePopulate, AuthenticationPopulate, V5cSearchPagePopulate, AreYouRegisteredPage, Helper}
import LoginCachePopulate._

class AuthenticationControllerSpec extends WordSpec with Matchers with Mockito {

  "Authentication - Controller" should {

    "present" in new WithApplication {
      // Arrange
      setupCache
      val request = FakeRequest().withSession()

      // Act
      val result = Authentication.present(request)

      // Assert
      status(result) should equal(OK)
    }

    "present login page when user is not logged in" in new WithApplication {
      // Arrange
      val request = FakeRequest().withSession()

      // Act
      val result = Authentication.present(request)

      // Assert
      redirectLocation(result) should equal(Some(AreYouRegisteredPage.url))
    }

    "redirect to next page after the button is clicked" in new WithApplication {
      // Arrange
      val request = FakeRequest().withSession()
        .withFormUrlEncodedBody(pinFormId -> Helper.pinValid)

      // Act
      val result = Authentication.submit(request)

      // Assert
      status(result) should equal(SEE_OTHER)
      redirectLocation(result) should equal (Some(V5cSearchPagePopulate.url))
    }

    "report bad request when no details are entered" in new WithApplication {
      // Arrange
      val request = FakeRequest().withSession()
        .withFormUrlEncodedBody()

      // Act
      val result = Authentication.submit(request)

      // Assert
      status(result) should equal(BAD_REQUEST)
    }
  }
}
