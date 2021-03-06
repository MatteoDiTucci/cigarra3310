package controllers

import domain.Level
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.api.http.Status.SEE_OTHER
import services.{CigarraService, LevelService}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CigarraControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraController" when {

    "receiving a valid POST request for creating a new Cigarra" should {

      "redirect to the Cigarra editor page" in {
        val request = FakeRequest("POST", "/").withFormUrlEncodedBody("name" -> "some-name")
        val cigarraService = mock[CigarraService]
        when(cigarraService.createCigarraWithName("some-name")).thenReturn("some-id")

        val controller = createController(cigarraService)

        val result = controller.create()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get must endWith("/cigarra/some-id/level")
        verify(cigarraService, times(1)).createCigarraWithName("some-name")
      }
    }

    "receiving a malformed POST request for creating a new Cigarra" should {
      val request = FakeRequest("POST", "/").withFormUrlEncodedBody()

      "return a BadRequest" in {
        val controller = createController()

        val result = controller.create()(request)

        status(result) mustEqual BAD_REQUEST
      }
    }

    "receiving a GET request for playing a Cigarra" when {

      "the Cigarra has at least one level" should {

        "redirect to the the first level of the Cigarra" in {
          val cigarraService = mock[CigarraService]
          when(cigarraService.findFirstLevel(any[String]))
            .thenReturn(Future.successful(Some(Level("some-level-id", "some-description", "some-solution"))))
          val controller = createController(cigarraService = cigarraService)

          val cigarraId = "some-cigarra-id"
          val request = FakeRequest("GET", s"/cigarra/$cigarraId")
          val result = controller.findFirstLevel(cigarraId)(request)

          status(result) mustEqual SEE_OTHER
        }
      }

      "the Cigarra has no level" should {

        "return an Internal Server error" in {
          val cigarraService = mock[CigarraService]
          when(cigarraService.findFirstLevel(any[String]))
            .thenReturn(Future.successful(None))
          val controller = createController(cigarraService = cigarraService)

          val cigarraId = "some-cigarra-id"
          val request = FakeRequest("GET", s"/cigarra/$cigarraId")
          val result = controller.findFirstLevel(cigarraId)(request)

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  private def createController(cigarraService: CigarraService = mock[CigarraService],
                               levelService: LevelService = mock[LevelService]) =
    new CigarraController(cigarraService, levelService)(Helpers.stubControllerComponents())
}
