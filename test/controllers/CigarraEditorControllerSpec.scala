package controllers

import domain.Cigarra
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test._
import services.{CigarraService, LevelService}
import org.scalatest.concurrent.Eventually.eventually

class CigarraEditorControllerSpec extends WordSpec with MustMatchers with MockitoSugar with Results {
  "CigarraEditorController" when {

    "a GET request for the Master editor page is made" when {

      "the Cigarra exists" should {

        "return the Master editor html" in {
          val cigarraName = "some-name"
          val cigarraGuid = "some-cigarra-guid"
          val cigarraService = mock[CigarraService]
          when(cigarraService.findCigarra(any[String])).thenReturn(Some(Cigarra(name = cigarraName)))
          val controller = createController(cigarraService = cigarraService)

          val result = controller.index(cigarraGuid)(FakeRequest())

          contentAsString(result) must include(cigarraName)
          contentAsString(result) must include(cigarraGuid)
        }
      }

      "the Cigarra does not exists" should {

        "return an Internal Error" in {
          val cigarraName = "some-name"
          val cigarraGuid = "some-cigarra-guid"
          val cigarraService = mock[CigarraService]
          when(cigarraService.findCigarra(any[String])).thenReturn(None)
          val controller = createController(cigarraService = cigarraService)

          val result = controller.index(cigarraGuid)(FakeRequest())

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "receiving a valid POST request to create a new Level" when {
      val cigarraGuid = "some-cigarra-guid"
      val request =
        FakeRequest("POST", s"/cigarra/$cigarraGuid/level").withFormUrlEncodedBody("description" -> "some-name",
                                                                                   "solution" -> "some-solution")

      "the Cigarra exists" should {

        "create a new level and return the Editor Page" in {
          val levelService = mock[LevelService]
          when(levelService.createLevel(any[String], any[String], any[String])).thenReturn(Some("some-level-guid"))

          val cigarraName = "some-name"
          val cigarraService = mock[CigarraService]
          when(cigarraService.findCigarra(any[String])).thenReturn(Some(Cigarra(name = cigarraName)))

          val controller = createController(cigarraService, levelService)

          val result = controller.createLevel("some-cigarra-guid")(request)

          contentAsString(result) must include(cigarraName)
          contentAsString(result) must include(cigarraGuid)
          contentAsString(result) must include("Feedback")

          eventually {
            verify(levelService, times(1)).createLevel(any[String], any[String], any[String])
          }
        }
      }

      "the Cigarra does not exist" should {

        "return a Bad Request" in {
          val cigarraService = mock[CigarraService]
          when(cigarraService.findCigarra(any[String])).thenReturn(None)
          val controller = createController(cigarraService)

          val result = controller.createLevel("bad-cigarra-guid")(request)

          status(result) mustBe BAD_REQUEST
        }

      }
    }

    "receiving a POST without level description" should {
      val request =
        FakeRequest("POST", s"/cigarra/some-cigarra-guid/level").withFormUrlEncodedBody("solution" -> "some-solution")

      "return a BadRequest" in {
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(any[String])).thenReturn(Some(Cigarra(name = "some-cigarra-name")))

        val controller = createController(cigarraService)

        val result = controller.createLevel("some-cigarra-guid")(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "receiving a POST without level solution" should {
      val request =
        FakeRequest("POST", s"/cigarra/some-cigarra-guid/level")
          .withFormUrlEncodedBody("description" -> "some-description")

      "return a BadRequest" in {
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(any[String])).thenReturn(Some(Cigarra(name = "some-cigarra-name")))

        val controller = createController(cigarraService)

        val result = controller.createLevel("some-cigarra-guid")(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "the Level cannot be created" should {

      "return an Internal Server error" in {
        val request =
          FakeRequest("POST", s"/cigarra/some-cigarra-guid/level")
            .withFormUrlEncodedBody("description" -> "some-description", "solution" -> "some-solution")

        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(any[String])).thenReturn(Some(Cigarra(name = "some-cigarra-name")))
        val levelService = mock[LevelService]
        when(levelService.createLevel(any[String], any[String], any[String])).thenReturn(None)

        val controller = createController(cigarraService, levelService)

        val result = controller.createLevel("some-cigarra-guid")(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  private def createController(cigarraService: CigarraService = mock[CigarraService],
                               levelService: LevelService = mock[LevelService]): CigarraEditorController =
    new CigarraEditorController(cigarraService, levelService)(Helpers.stubControllerComponents())

}
