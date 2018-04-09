package controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.api.http.Status.SEE_OTHER
import services.CigarraService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

class CigarraCreationControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraCreationController" when {

    "receiving a valid POST request for creating a new Cigarra" when {

      "the CigarraService is able to create a new Cigarra" should {

        "redirect to the Master editor page" in {
          val request = FakeRequest("POST", "/").withFormUrlEncodedBody("name" -> "some-name")
          val cigarraService = mock[CigarraService]
          when(cigarraService.createCigarra(any[String])).thenReturn(Some("some-id"))

          val controller = createController(cigarraService)

          val result = controller.create()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).get must endWith("/cigarra/some-id/editor")
        }
      }

      "the CigarraService is not able to create a new Cigarra" should {

        "return an Internal Server error" in {
          val request = FakeRequest("POST", "/").withFormUrlEncodedBody("name" -> "some-name")
          val cigarraService = mock[CigarraService]
          when(cigarraService.createCigarra(any[String])).thenReturn(None)

          val controller = createController(cigarraService)

          val result = controller.create()(request)

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "receiving an invalid POST request for creating a new Cigarra" should {
        val request = FakeRequest("POST", "/").withFormUrlEncodedBody()

        "return a BadRequest" in {
          val controller = createController()

          val result = controller.create()(request)

          status(result) mustEqual BAD_REQUEST
        }
      }
    }
  }

  private def createController(cigarraService: CigarraService = mock[CigarraService]) =
    new CigarraCreationController(cigarraService)(Helpers.stubControllerComponents())
}
