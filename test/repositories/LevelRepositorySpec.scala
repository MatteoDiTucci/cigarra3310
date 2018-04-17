package repositories

import java.util.UUID

import domain.Level
import org.scalatest.{MustMatchers, WordSpec}

import scala.collection.mutable.ListBuffer

class LevelRepositorySpec extends WordSpec with MustMatchers {
  "LevelRepository" when {

    "receiving a Cigarra guid and a level to persist a Level" should {

      "persist the level and return its guid" in {
        val repository = new LevelRepository()
        val cigarraGuid = UUID.fromString("24c672c2-589c-4728-a4c3-0be50a269918")

        val levelGuid = repository.createLevel(cigarraGuid.toString,
                                               Level(description = "some-description", solution = "some-solution"))

        levelGuid mustBe defined
        repository.cigarrasLevels(cigarraGuid).exists { level: Level =>
          level.guid.toString.equals(levelGuid.get)
        }
      }
    }

    "receiving a Cigarra guid to retrieve its first level " when {

      "the level exists" should {

        "return the Level" in {
          val cigarraGuid = UUID.fromString("24c672c2-589c-4728-a4c3-0be50a269918")
          val expectedLevel = Level(None, "some-description", "some-solution")
          val repository = new LevelRepository()
          repository.cigarrasLevels.put(cigarraGuid, ListBuffer(expectedLevel))

          val result = repository.findFirstLevel(cigarraGuid.toString)

          result mustBe Some(expectedLevel)
        }
      }
    }

    "receiving a Cigarra guid and a level guid to find a Level" when {

      "the Level exist" should {

        "return the Level" in {
          val cigarraGuid = UUID.fromString("24c672c2-589c-4728-a4c3-0be50a269918")
          val levelGuid = "13b497c2-ab38-1098-b863-abc13459573a"
          val expectedLevel = Level(Some(levelGuid), "some-description", "some-solution")
          val repository = new LevelRepository()
          repository.cigarrasLevels.put(cigarraGuid, ListBuffer(expectedLevel))

          val result = repository.findLevel(cigarraGuid.toString, levelGuid)

          result mustBe Some(expectedLevel)
        }
      }

      "the Level does not exist" should {

        "return None" in {
          val cigarraGuid = UUID.fromString("24c672c2-589c-4728-a4c3-0be50a269918")
          val levelGuid = "13b497c2-ab38-1098-b863-abc13459573a"
          val repository = new LevelRepository()

          val result = repository.findLevel(cigarraGuid.toString, levelGuid)

          result mustBe None
        }
      }
    }

    "receiving a Cigarra guid and a level guid to find the next Level" when {

      "the Level to find exists and it is not the last one" should {

        "return the next Level" in {
          val cigarraGuid = UUID.fromString("24c672c2-589c-4728-a4c3-0be50a269918")
          val currentLevel = Level(Some("some-guid"), "some-description", "some-solution")
          val nextLevel = Level(Some("next-level-guid"), "next-level-description", "next-level-solution")
          val repository = new LevelRepository()
          repository.cigarrasLevels.put(cigarraGuid, ListBuffer(currentLevel, nextLevel))

          val result = repository.findNextLevel(cigarraGuid.toString, "some-guid")

          result mustBe Some(nextLevel)
        }
      }

      "the Level to find exists and it is the last one" should {

        "return None" in {
          val cigarraGuid = UUID.fromString("24c672c2-589c-4728-a4c3-0be50a269918")
          val level = Level(Some("some-guid"), "some-description", "some-solution")
          val repository = new LevelRepository()
          repository.cigarrasLevels.put(cigarraGuid, ListBuffer(level))

          val result = repository.findNextLevel(cigarraGuid.toString, "some-guid")

          result mustBe None
        }
      }

      "the Level to find" +
        " does not exist" should {

        "return None" in {
          val cigarraGuid = UUID.fromString("24c672c2-589c-4728-a4c3-0be50a269918")
          val currentLevel = Level(Some("some-guid"), "some-description", "some-solution")
          val repository = new LevelRepository()
          repository.cigarrasLevels.put(cigarraGuid, ListBuffer(currentLevel))

          val result = repository.findNextLevel(cigarraGuid.toString, "not-existing-guid")

          result mustBe None
        }
      }
    }

  }
}
