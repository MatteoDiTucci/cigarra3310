package controllers

import domain.{Cigarra, Level}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.mvc.Results
import play.api.test._
import services.{CigarraService, LevelService}
import org.mockito.Mockito._
import scala.concurrent.duration._

import scala.concurrent.Await

class LevelControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "LevelController" when {

    "receiving a GET request to play a Cigarra Level" should {

      "return the play Level page" in {
        val cigarra = Cigarra(Some("some-cigarra-guid"), "some-cigarra-name")
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(cigarra.guid.get)).thenReturn(Some(cigarra))

        val firstLevel = Level(Some("some-level-guid"), "some-cigarra-name", "some-solution")
        val levelService = mock[LevelService]
        when(levelService.findFirstLevel(cigarra.guid.get)).thenReturn(Some(firstLevel))

        val controller = new LevelController(cigarraService, levelService)(Helpers.stubControllerComponents())

        val result = Await.result(controller.level(cigarra.guid.get, firstLevel.guid.get)(FakeRequest()), 1.second)

        result mustEqual Results.Ok(
          views.html.level(cigarra.guid.get, cigarra.name, firstLevel.guid.get, firstLevel.description))
      }

    }
  }
}
