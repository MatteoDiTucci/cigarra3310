package repositories

import anorm.SQL
import domain.Level
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import play.api.db.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class LevelRepositorySpec extends WordSpec with MustMatchers with BeforeAndAfterEach {

  private val cigarraId = "some-cigarra-id"
  private val levelId = "some-level-id"
  private val anotherLevelId = "another-level-id"

  override def beforeEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteLevel(database, levelId), 1.second)
    Await.result(deleteLevel(database, anotherLevelId), 1.second)
  }

  override def afterEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteLevel(database, levelId), 1.second)
    Await.result(deleteLevel(database, anotherLevelId), 1.second)
  }

  "LevelRepository" when {

    "receiving a Cigarra id and a Level to save a Level" should {

      "persist the level" in {
        DbFixtures.withMyDatabase { database =>
          val repository = new LevelRepository(database)
          val level = createAndSaveLevelIn(repository)

          Await.result(repository.find(levelId), 1.second) mustEqual level
        }
      }
    }

    "receiving a Cigarra id to find its last created Level" when {

      "the Cigarra has one level" should {

        "return the id of the level" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)
            val level = createAndSaveLevelIn(repository)

            Await.result(repository.findLastCreatedLevelId(cigarraId), 1.second) mustBe Some(level.id)
          }
        }
      }

      "the Cigarra has two levels" should {

        "return the id of the last one" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)
            val first = createLevelWithId(levelId)
            val last = createLevelWithId(anotherLevelId)
            saveTwoLevelsForOneCigarra(first, last, repository)

            Await.result(repository.findLastCreatedLevelId(cigarraId), 1.second) mustBe Some(last.id)
          }
        }
      }

      "the cigarra has no level" should {

        "return nothing" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)

            Await.result(repository.findLastCreatedLevelId(cigarraId), 1.second) mustBe None
          }
        }
      }
    }

    "receiving two level ids to link them" should {

      "connect the previous level to the new" in {
        DbFixtures.withMyDatabase { database =>
          val repository = new LevelRepository(database)
          val previousLevel = createLevelWithId(levelId)
          val newLevel = createLevelWithId(anotherLevelId)
          saveTwoLevelsForOneCigarra(previousLevel, newLevel, repository)

          Await.result(repository.findNext(previousLevel.id), 1.second).get mustEqual newLevel
        }
      }
    }

    "receiving an id to retrieve a level" should {

      "return the Level" in {
        DbFixtures.withMyDatabase { database =>
          val level = createLevelWithId(levelId)
          val repository = new LevelRepository(database)
          Await.result(saveLevel(level, repository), 1.second)

          Await.result(repository.find(level.id), 1.second) mustBe level
        }
      }
    }

    "receiving an id to find the next level" when {

      "there is a next level" should {

        "return the next Level" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)
            val previousLevel = createLevelWithId(levelId)
            val newLevel = createLevelWithId(anotherLevelId)
            saveTwoLevelsForOneCigarra(previousLevel, newLevel, repository)

            Await.result(repository.findNext(previousLevel.id), 1.second) mustBe Some(newLevel)
          }
        }
      }

      "there is no next level" should {

        "return nothing" in {
          DbFixtures.withMyDatabase { database =>
            val repository = new LevelRepository(database)
            val firstLevel = createLevelWithId(levelId)
            Await.result(saveLevel(firstLevel, repository), 1.second)

            Await.result(repository.findNext(firstLevel.id), 1.second) mustBe None
          }
        }
      }
    }

  }

  private def saveLevel(level: Level, repository: LevelRepository): Future[Boolean] =
    repository.save(levelGuid = level.id,
                    description = level.description,
                    solution = level.solution,
                    cigarraGuid = cigarraId)

  private def createAndSaveLevelIn(repository: LevelRepository): Level = {
    val level = createLevelWithId(levelId)
    Await.result(repository.save(levelGuid = level.id,
                                 description = level.description,
                                 solution = level.solution,
                                 cigarraGuid = cigarraId),
                 1.second)
    level
  }

  private def createLevelWithId(id: String) =
    Level(id = id, description = "some-description", solution = "some-solution")

  def saveTwoLevelsForOneCigarra(previous: Level, next: Level, repository: LevelRepository): Boolean =
    Await.result(for {
      _ <- saveLevel(previous, repository)
      __ <- saveLevel(next, repository)
      ___ <- repository.linkToPreviousLevel(next.id, previous.id)
    } yield true, 1.second)

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
