package repositories

import java.util.UUID

import domain.Level
import org.scalatest.{MustMatchers, WordSpec}

class LevelRepositorySpec extends WordSpec with MustMatchers {
  "LevelRepository" when {

    "receiving a Cigarra guid and a level" should {

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
  }
}
