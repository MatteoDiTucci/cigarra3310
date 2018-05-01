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
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class CigarraEditorControllerSpec extends WordSpec with MustMatchers with MockitoSugar with Results {
  "CigarraEditorController" when {

    "a GET request for the Cigarra editor page is made" should {

      "return the Cigarra editor html" in {
        val cigarraName = "some-name"
        val cigarraGuid = "some-cigarra-guid"
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(any[String]))
          .thenReturn(Future.successful(Cigarra(guid = "some-guid", name = cigarraName)))
        val controller = createController(cigarraService = cigarraService)

        val result = controller.levelEditor(cigarraGuid)(FakeRequest())

        contentAsString(result) must include(cigarraName)
        contentAsString(result) must include(cigarraGuid)
      }
    }

    "receiving a valid POST request to create a new Level" should {

      "create a new level and return the Editor Page" in {
        val cigarraGuid = "some-cigarra-guid"
        val request =
          FakeRequest("POST", s"/cigarra/$cigarraGuid/level").withFormUrlEncodedBody("description" -> "some-name",
                                                                                     "solution" -> "some-solution")

        val levelService = mock[LevelService]
        when(levelService.createLevel(any[String], any[String], any[String]))
          .thenReturn(Future.successful("level-guid"))

        val cigarraName = "some-name"
        val cigarraService = mock[CigarraService]
        val cigarra = Cigarra(guid = "some-guid", name = cigarraName)
        when(cigarraService.findCigarra(any[String])).thenReturn(Future.successful(cigarra))

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

    "receiving a malformed POST" when {
      val cigarra = Cigarra(guid = "some-guid", name = "some-cigarra-name")
      val cigarraService = mock[CigarraService]
      when(cigarraService.findCigarra(any[String])).thenReturn(Future.successful(cigarra))
      val controller = createController(cigarraService)

      "the request has no level description" should {
        val request =
          FakeRequest("POST", s"/cigarra/some-cigarra-guid/level").withFormUrlEncodedBody("solution" -> "some-solution")

        "return a BadRequest" in {
          val result = controller.createLevel("some-cigarra-guid")(request)

          status(result) mustBe BAD_REQUEST
        }
      }

      "request has no level solution" should {
        val request =
          FakeRequest("POST", s"/cigarra/some-cigarra-guid/level")
            .withFormUrlEncodedBody("description" -> "some-description")

        "return a BadRequest" in {
          val result = controller.createLevel("some-cigarra-guid")(request)

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }

  private def createController(cigarraService: CigarraService = mock[CigarraService],
                               levelService: LevelService = mock[LevelService]): CigarraEditorController =
    new CigarraEditorController(cigarraService, levelService)(Helpers.stubControllerComponents())

}
