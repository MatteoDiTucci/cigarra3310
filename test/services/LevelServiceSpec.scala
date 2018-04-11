package services

import domain.Level
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import repositories.LevelRepository
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

class LevelServiceSpec extends WordSpec with MockitoSugar with MustMatchers {
  "LevelService" when {

    "receiving description and solution for a level" should {

      "create a level and return its guid" in {
        val expectedGuid = "some-guid"
        val levelRepository = mock[LevelRepository]
        when(levelRepository.createLevel(any[String], any[Level])).thenReturn(Some(expectedGuid))
        val service = new LevelService(levelRepository)

        val guid = service.createLevel("some-cigarra-guid", "some-description", "some-solution")

        guid mustBe Some(expectedGuid)
      }
    }

    "the level cannot be created" should {

      "return a None" in {
        val levelRepository = mock[LevelRepository]
        when(levelRepository.createLevel(any[String], any[Level])).thenReturn(None)
        val service = new LevelService(levelRepository)

        val guid = service.createLevel("some-cigarra-guid", "some-description", "some-solution")

        guid mustBe None
      }
    }
  }
}
