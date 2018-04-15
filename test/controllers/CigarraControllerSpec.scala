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

class CigarraControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraController" when {

    "receiving a valid POST request for creating a new Cigarra" when {

      "it is possible to create a new Cigarra" should {

        "redirect to the Master editor page" in {
          val request = FakeRequest("POST", "/").withFormUrlEncodedBody("name" -> "some-name")
          val cigarraService = mock[CigarraService]
          when(cigarraService.createCigarra(any[String])).thenReturn(Some("some-id"))

          val controller = createController(cigarraService)

          val result = controller.create()(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).get must endWith("/cigarra/some-id/level")
          verify(cigarraService, times(1)).createCigarra(any[String])
        }
      }

      "it is not possible to create a new Cigarra" should {

        "return an Internal Server error" in {
          val request = FakeRequest("POST", "/").withFormUrlEncodedBody("name" -> "some-name")
          val cigarraService = mock[CigarraService]
          when(cigarraService.createCigarra(any[String])).thenReturn(None)

          val controller = createController(cigarraService)

          val result = controller.create()(request)

          status(result) mustEqual INTERNAL_SERVER_ERROR
          verify(cigarraService, times(1)).createCigarra(any[String])
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
    "receiving a GET request for playing a Cigarra" when {

      "the Cigarra exists" should {

        "redirect to the the first level of the Cigarra" in {
          val levelService = mock[LevelService]
          when(levelService.findFirstLevel(any[String]))
            .thenReturn(Some(Level(Some("some-level-guid"), "some-description", "some-solution")))
          val controller = createController(levelService = levelService)

          val cigarraGuid = "some-cigarra-guid"
          val request = FakeRequest("GET", s"/cigarra/$cigarraGuid")
          val result = controller.findFirstLevel(cigarraGuid)(request)

          status(result) mustEqual SEE_OTHER
        }
      }

      "the Cigarra does not exist" should {

        "return a Bad Request" in {
          val levelService = mock[LevelService]
          when(levelService.findFirstLevel(any[String]))
            .thenReturn(None)
          val controller = createController(levelService = levelService)

          val cigarraGuid = "some-cigarra-guid"
          val request = FakeRequest("GET", s"/cigarra/$cigarraGuid")
          val result = controller.findFirstLevel(cigarraGuid)(request)

          status(result) mustEqual BAD_REQUEST
        }
      }
    }
  }

  private def createController(cigarraService: CigarraService = mock[CigarraService],
                               levelService: LevelService = mock[LevelService]) =
    new CigarraController(cigarraService, levelService)(Helpers.stubControllerComponents())
}
