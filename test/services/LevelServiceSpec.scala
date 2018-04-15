package services

import domain.Level
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import repositories.LevelRepository
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

class LevelServiceSpec extends WordSpec with MockitoSugar with MustMatchers {
  "LevelService" when {

    "receiving description and solution for creating a level" when {

      "the level can be created" should {

        "return its guid" in {
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

    "receiving a Cigarra guid to retrieve its first level" when {

      "the level exists" should {

        "return the Level" in {
          val expectedLevel = Level(Some("level-guid"), "description", "solution")
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findFirstLevel(any[String]))
            .thenReturn(Some(expectedLevel))
          val service = new LevelService(levelRepository)

          val guid = service.findFirstLevel("some-cigarra-guid")

          guid mustBe Some(expectedLevel)
        }
      }

      "the level does not exist" should {

        "return None" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findFirstLevel(any[String]))
            .thenReturn(None)
          val service = new LevelService(levelRepository)

          val guid = service.findFirstLevel("some-cigarra-guid")

          guid mustBe None
        }
      }
    }
  }
}
