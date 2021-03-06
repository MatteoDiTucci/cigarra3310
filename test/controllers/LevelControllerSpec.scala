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
import play.api.http.Status.BAD_REQUEST
import play.api.test.Helpers._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class LevelControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "LevelController" when {

    "receiving a GET request to play a Cigarra Level" should {

      "return the Level page" in {
        val cigarra = Cigarra("some-cigarra-id", "some-cigarra-name")
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(cigarra.id)).thenReturn(Future.successful(cigarra))

        val level = Level("some-level-id", "some-cigarra-name", "some-solution")
        val levelService = mock[LevelService]
        when(levelService.findLevel(level.id)).thenReturn(Future.successful(level))

        val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

        val result = Await.result(controller.level(cigarra.id, level.id)(FakeRequest()), 1.second)

        result mustEqual Results.Ok(views.html.level(cigarra.id, cigarra.name, level.id, level.description))
      }
    }

    "receiving a POST request to solve a Level" when {
      val cigarra = Cigarra("some-cigarra-id", "some-cigarra-name")
      val cigarraService = mock[CigarraService]
      when(cigarraService.findCigarra(any[String])).thenReturn(Future.successful(cigarra))

      "the solution is correct and the current level is not the final one" should {

        "redirect to the next level" in {
          val request =
            FakeRequest("POST", "/cigarra/some-cigarra-id/level/current-level-id")
              .withFormUrlEncodedBody("solution" -> "current-level-solution")

          val nextLevel = Level("next-level-id", "next-level-description", "next-level-solution")
          val levelService = mock[LevelService]
          when(levelService.solveLevel(any[String], any[String], any[String]))
            .thenReturn(Future.successful(true))
          when(levelService.findNextLevel(any[String]))
            .thenReturn(Future.successful(Some(nextLevel)))

          val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

          val result =
            Await.result(controller.solveLevel(cigarra.id, "current-level-id")(request), 1.second)

          result mustEqual Results.SeeOther("/cigarra/some-cigarra-id/level/next-level-id")
        }
      }

      "the solution is correct and the next level is the final one" should {

        "redirect to the finish page" in {
          val request =
            FakeRequest("POST", "/cigarra/some-cigarra-id/level/current-level-id")
              .withFormUrlEncodedBody("solution" -> "current-level-solution")

          val levelService = mock[LevelService]
          when(levelService.solveLevel(any[String], any[String], any[String]))
            .thenReturn(Future.successful(true))
          when(levelService.findNextLevel(any[String]))
            .thenReturn(Future.successful(None))

          val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

          val result = controller.solveLevel(cigarra.id, "current-level-id")(request)

          status(result) mustBe OK
          contentAsString(result) must include("The End")
        }
      }

      "the solution is not correct" should {

        "redirect to the current level" in {
          val request =
            FakeRequest("POST", "/cigarra/some-cigarra-id/level/current-level-id")
              .withFormUrlEncodedBody("solution" -> "bad-solution")

          val levelService = mock[LevelService]
          when(levelService.solveLevel(any[String], any[String], any[String]))
            .thenReturn(Future.successful(false))

          val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

          val result =
            Await.result(controller.solveLevel(cigarra.id, "current-level-id")(request), 1.second)

          result mustEqual Results.SeeOther("/cigarra/some-cigarra-id/level/current-level-id")
        }
      }

      "the request does not contain the solution" should {

        "return Bad Request" in {
          val request =
            FakeRequest("POST", "/cigarra/some-cigarra-id/level/current-level-id")
              .withFormUrlEncodedBody()

          val controller = new LevelController(cigarraService, mock[LevelService])(Helpers.stubControllerComponents())

          val result = controller.solveLevel(cigarra.id, "current-level-id")(request)

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }
}
