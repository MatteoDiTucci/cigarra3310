package repositories

import anorm.SQL
import domain.Level
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpec}
import play.api.db.Database

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LevelRepositorySpec extends WordSpec with MustMatchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteLevel(database, "some-guid"), 1.second)
    Await.result(deleteLevel(database, "another-guid"), 1.second)
  }

  override def afterEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteLevel(database, "some-guid"), 1.second)
    Await.result(deleteLevel(database, "another-guid"), 1.second)
  }

  "LevelRepository" when {

    "receiving a Cigarra guid and a Level to save a Level" should {

      "persist the level" in {
        DbFixtures.withMyDatabase { database =>
          val level = Level(guid = "some-guid", description = "some-description", solution = "some-solution")
          val repository = new LevelRepository(database)

          Await.result(repository.save(level.guid, level.description, level.solution, "some-cigarra-guid"), 1.second)

          Await.result(repository.find("some-guid"), 1.second) mustEqual level
        }
      }
    }

    "receiving a Cigarra guid to find its last created Level" when {

      "the Cigarra has one level" should {

        "return the guid of the level" in {
          DbFixtures.withMyDatabase { database =>
            val level = Level(guid = "some-guid", description = "some-description", solution = "some-solution")
            val repository = new LevelRepository(database)
            Await.result(
              repository.save(levelGuid = level.guid,
                              description = level.description,
                              solution = level.solution,
                              cigarraGuid = "some-cigarra-guid"),
              1.second
            )

            Await.result(repository.findLastCreatedLevelGuid("some-cigarra-guid"), 1.second) mustBe Some("some-guid")
          }
        }
      }

      "the Cigarra has two levels" should {

        "return the guid of the first one" in {
          DbFixtures.withMyDatabase { database =>
            val previousLevel = Level(guid = "some-guid", description = "some-description", solution = "some-solution")
            val repository = new LevelRepository(database)
            Await.result(
              repository.save(levelGuid = previousLevel.guid,
                              description = previousLevel.description,
                              solution = previousLevel.solution,
                              cigarraGuid = "some-cigarra-guid"),
              1.second
            )

            val newLevel =
              Level(guid = "another-guid", description = "another-description", solution = "another-solution")
            Await.result(
              repository.save(levelGuid = newLevel.guid,
                              description = newLevel.description,
                              solution = newLevel.solution,
                              cigarraGuid = "some-cigarra-guid"),
              1.second
            )
            Await.result(repository.linkToPreviousLevel(newLevel.guid, previousLevel.guid), 1.second)

            Await.result(repository.findLastCreatedLevelGuid("some-cigarra-guid"), 1.second) mustBe Some("another-guid")
          }
        }
      }

      "the Cigarra has no level" should {

        "return None" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)

            Await.result(repository.findLastCreatedLevelGuid("some-cigarra-guid"), 1.second) mustBe None
          }
        }
      }
    }

    "receiving two level guid to link them" should {

      "connect the previous level to the new next" in {
        DbFixtures.withMyDatabase { database =>
          val repository = new LevelRepository(database)

          val previousLevel = Level(guid = "some-guid", description = "first-description", solution = "first-solution")
          Await.result(repository.save(previousLevel.guid,
                                       description = previousLevel.description,
                                       solution = previousLevel.solution,
                                       cigarraGuid = "some-cigarra-guid"),
                       1.second)

          val newLevel =
            Level(guid = "another-guid", description = "second-description", solution = "second-solution")
          Await.result(
            repository.save(levelGuid = newLevel.guid,
                            description = newLevel.description,
                            solution = newLevel.solution,
                            cigarraGuid = "some-cigarra-guid"),
            1.second
          )

          Await.result(repository.linkToPreviousLevel(newLevel.guid, previousLevel.guid), 1.second)

          Await.result(repository.findNext(previousLevel.guid), 1.second).get mustEqual newLevel
        }
      }
    }

    "receiving a Level guid to retrieve a Level" should {

      "return the Level" in {
        DbFixtures.withMyDatabase { database =>
          val level = Level(guid = "some-guid", description = "some-description", solution = "some-solution")
          val repository = new LevelRepository(database)
          Await.result(repository.save(levelGuid = level.guid,
                                       description = level.description,
                                       solution = level.solution,
                                       cigarraGuid = "some-cigarra-guid"),
                       1.second)

          Await.result(repository.find(level.guid), 1.second) mustBe level
        }
      }
    }

    "receiving a Level guid to find the next Level" when {

      "the Level to find exists" should {

        "return the next Level" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)

            val previousLevel =
              Level(guid = "some-guid", description = "first-description", solution = "first-solution")
            Await.result(
              repository.save(levelGuid = previousLevel.guid,
                              description = previousLevel.description,
                              solution = previousLevel.solution,
                              cigarraGuid = "some-cigarra-guid"),
              1.second
            )

            val newLevel =
              Level(guid = "another-guid", description = "second-description", solution = "second-solution")
            Await.result(
              repository.save(levelGuid = newLevel.guid,
                              description = newLevel.description,
                              solution = newLevel.solution,
                              cigarraGuid = "some-cigarra-guid"),
              1.second
            )
            Await.result(repository.linkToPreviousLevel(newLevel.guid, previousLevel.guid), 1.second)

            Await.result(repository.findNext(previousLevel.guid), 1.second) mustBe Some(newLevel)
          }
        }
      }

      "the Level to find does not exist" should {

        "return None" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)

            val firstLevel = Level(guid = "some-guid", description = "first-description", solution = "first-solution")
            Await.result(repository.save(levelGuid = firstLevel.guid,
                                         description = firstLevel.description,
                                         solution = firstLevel.solution,
                                         cigarraGuid = "some-cigarra-guid"),
                         1.second)

            Await.result(repository.findNext(firstLevel.guid), 1.second) mustBe None
          }
        }
      }
    }

  }

  def deleteLevel(db: Database, guid: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                DELETE FROM level
                WHERE guid = {guid};
          """
        ).on(
            'guid -> guid
          )
          .executeInsert()
      }
    }
}
