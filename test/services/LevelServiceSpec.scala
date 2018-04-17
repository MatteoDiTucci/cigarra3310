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

    "receiving a solution for a Level" when {
      val levelRepository = mock[LevelRepository]
      when(levelRepository.findLevel(any[String], any[String]))
        .thenReturn(Some(Level(Some("current-level-guid"), "some-description", "current-level-solution")))

      "the solution is correct" should {

        "return true" in {
          val level = Level(Some("some-guid"), "some-description", "some-solution")
          when(levelRepository.findLevel(any[String], any[String]))
            .thenReturn(Some(level))

          val service = new LevelService(levelRepository)

          val result = service.solveLevel("cigarra-guid", "some-guid", "some-solution")

          result mustBe Some(true)
        }
      }

      "the solution is not correct" should {

        "return false" in {
          val level = Level(Some("some-guid"), "some-description", "some-solution")
          when(levelRepository.findLevel(any[String], any[String]))
            .thenReturn(Some(level))

          val service = new LevelService(levelRepository)

          val result = service.solveLevel("cigarra-guid", "some-guid", "bad-solution")

          result mustBe Some(false)
        }
      }

      "the Level cannot be found" should {

        "return false" in {
          when(levelRepository.findLevel(any[String], any[String]))
            .thenReturn(None)

          val service = new LevelService(levelRepository)

          val result = service.solveLevel("cigarra-guid", "some-guid", "bad-solution")

          result mustBe None
        }
      }
    }

    "receiving a Cigarra and a Level guid to retrieve a Level" when {

      "the Level exists" should {

        "return the Level" in {
          val levelRepository = mock[LevelRepository]
          val level = Level(Some("some-level-guid"), "some-level-description", "some-level-solution")
          when(levelRepository.findLevel(any[String], any[String]))
            .thenReturn(Some(level))

          val service = new LevelService(levelRepository)

          val result = service.findLevel("cigarra-guid", "current-level-guid")

          result mustBe Some(level)
        }
      }

      "the Level does not exist" should {

        "return None" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findLevel(any[String], any[String]))
            .thenReturn(None)

          val service = new LevelService(levelRepository)

          val result = service.findLevel("cigarra-guid", "current-level-guid")

          result mustBe None
        }
      }
    }

    "receiving a Cigarra and a Level guid to retrieve the next Level" when {

      "the next level exists" should {

        "return the next Level" in {
          val levelRepository = mock[LevelRepository]
          val level = Level(Some("next-level-guid"), "next-level-description", "next-level-solution")
          when(levelRepository.findNextLevel(any[String], any[String]))
            .thenReturn(Some(level))

          val service = new LevelService(levelRepository)

          val result = service.findNextLevel("cigarra-guid", "current-level-guid")

          result mustBe Some(level)
        }
      }

      "the next level does not exist" should {

        "return None" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findNextLevel(any[String], any[String]))
            .thenReturn(None)

          val service = new LevelService(levelRepository)

          val result = service.findNextLevel("cigarra-guid", "current-level-guid")

          result mustBe None
        }
      }
    }
  }
}
