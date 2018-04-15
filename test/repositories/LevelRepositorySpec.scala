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

          val level = repository.findFirstLevel(cigarraGuid.toString)

          level mustBe Some(expectedLevel)
        }
      }
    }
  }
}
