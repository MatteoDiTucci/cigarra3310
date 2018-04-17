package controllers

import domain.{Cigarra, Level}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.mvc.Results
import play.api.test._
import services.{CigarraService, LevelService}
import org.mockito.Mockito._
import scala.concurrent.duration._
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Await

class LevelControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "LevelController" when {

    "receiving a GET request to play a Cigarra Level" should {

      "return the play Level page" in {
        val cigarra = Cigarra(Some("some-cigarra-guid"), "some-cigarra-name")
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(cigarra.guid.get)).thenReturn(Some(cigarra))

        val level = Level(Some("some-level-guid"), "some-cigarra-name", "some-solution")
        val levelService = mock[LevelService]
        when(levelService.findLevel(cigarra.guid.get, level.guid.get)).thenReturn(Some(level))

        val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

        val result = Await.result(controller.level(cigarra.guid.get, level.guid.get)(FakeRequest()), 1.second)

        result mustEqual Results.Ok(views.html.level(cigarra.guid.get, cigarra.name, level.guid.get, level.description))
      }
    }

    "receiving a POST request to submit the solution of a Cigarra Level" when {
      val cigarra = Cigarra(Some("some-cigarra-guid"), "some-cigarra-name")
      val cigarraService = mock[CigarraService]
      when(cigarraService.findCigarra(any[String])).thenReturn(Some(cigarra))

      "the solution is correct" should {

        "redirect to the next level" in {
          val request =
            FakeRequest("POST", "/cigarra/some-cigarra-guid/level/current-level-guid")
              .withFormUrlEncodedBody("solution" -> "current-level-solution")

          val nextLevel = Level(Some("next-level-guid"), "next-level-description", "next-level-solution")
          val levelService = mock[LevelService]
          when(levelService.solveLevel(any[String], any[String], any[String]))
            .thenReturn(Some(nextLevel))

          val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

          val result =
            Await.result(controller.solveLevel(cigarra.guid.get, "current-level-guid")(request), 1.second)

          result mustEqual Results.SeeOther("/cigarra/some-cigarra-guid/level/next-level-guid")
        }
      }

      "the solution is correct" should {

        "redirect to the current level" in {
          val request =
            FakeRequest("POST", "/cigarra/some-cigarra-guid/level/current-level-guid")
              .withFormUrlEncodedBody("solution" -> "bad-solution")

          val levelService = mock[LevelService]
          when(levelService.solveLevel(any[String], any[String], any[String]))
            .thenReturn(None)

          val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

          val result =
            Await.result(controller.solveLevel(cigarra.guid.get, "current-level-guid")(request), 1.second)

          result mustEqual Results.SeeOther("/cigarra/some-cigarra-guid/level/current-level-guid")
        }
      }
    }
  }
}
