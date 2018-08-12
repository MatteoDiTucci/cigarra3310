package dao

import anorm.SQL
import domain.Level
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import play.api.db.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class LevelDaoSpec extends WordSpec with MustMatchers with BeforeAndAfterEach {

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

  "LevelDao" when {

    "receiving a Cigarra id and a Level to save a Level" should {

      "persist the level" in {
        DbFixtures.withMyDatabase { database =>
          val dao = new LevelDao(database)
          val level = createAndSaveLevelIn(dao)

          Await.result(dao.find(levelId), 1.second) mustEqual level
        }
      }
    }

    "receiving a Cigarra id to find its last created Level" when {

      "the Cigarra has one level" should {

        "return the id of the level" in {
          DbFixtures.withMyDatabase { database =>
            val dao = new LevelDao(database)
            val level = createAndSaveLevelIn(dao)

            Await.result(dao.findLastCreatedLevelId(cigarraId), 1.second) mustBe Some(level.id)
          }
        }
      }

      "the Cigarra has two levels" should {

        "return the id of the last one" in {
          DbFixtures.withMyDatabase { database =>
            val dao = new LevelDao(database)
            val first = createLevelWithId(levelId)
            val last = createLevelWithId(anotherLevelId)
            saveTwoLevelsForOneCigarra(first, last, dao)

            Await.result(dao.findLastCreatedLevelId(cigarraId), 1.second) mustBe Some(last.id)
          }
        }
      }

      "the cigarra has no level" should {

        "return nothing" in {
          DbFixtures.withMyDatabase { database =>
            val dao = new LevelDao(database)

            Await.result(dao.findLastCreatedLevelId(cigarraId), 1.second) mustBe None
          }
        }
      }
    }

    "receiving two level ids to link them" should {

      "connect the previous level to the new" in {
        DbFixtures.withMyDatabase { database =>
          val dao = new LevelDao(database)
          val previousLevel = createLevelWithId(levelId)
          val newLevel = createLevelWithId(anotherLevelId)
          saveTwoLevelsForOneCigarra(previousLevel, newLevel, dao)

          Await.result(dao.findNext(previousLevel.id), 1.second).get mustEqual newLevel
        }
      }
    }

    "receiving an id to retrieve a level" should {

      "return the Level" in {
        DbFixtures.withMyDatabase { database =>
          val level = createLevelWithId(levelId)
          val dao = new LevelDao(database)
          Await.result(saveLevel(level, dao), 1.second)

          Await.result(dao.find(level.id), 1.second) mustBe level
        }
      }
    }

    "receiving an id to find the next level" when {

      "there is a next level" should {

        "return the next Level" in {
          DbFixtures.withMyDatabase { database =>
            val dao = new LevelDao(database)
            val previousLevel = createLevelWithId(levelId)
            val newLevel = createLevelWithId(anotherLevelId)
            saveTwoLevelsForOneCigarra(previousLevel, newLevel, dao)

            Await.result(dao.findNext(previousLevel.id), 1.second) mustBe Some(newLevel)
          }
        }
      }

      "there is no next level" should {

        "return nothing" in {
          DbFixtures.withMyDatabase { database =>
            val dao = new LevelDao(database)
            val firstLevel = createLevelWithId(levelId)
            Await.result(saveLevel(firstLevel, dao), 1.second)

            Await.result(dao.findNext(firstLevel.id), 1.second) mustBe None
          }
        }
      }
    }

  }

  private def saveLevel(level: Level, dao: LevelDao): Future[Boolean] =
    dao.save(cigarraId, level)

  private def createAndSaveLevelIn(dao: LevelDao): Level = {
    val level = createLevelWithId(levelId)
    Await.result(dao.save(cigarraId, level), 1.second)
    level
  }

  private def createLevelWithId(id: String) =
    Level(id = id, description = "some-description", solution = "some-solution")

  def saveTwoLevelsForOneCigarra(previous: Level, next: Level, dao: LevelDao): Boolean =
    Await.result(for {
      _ <- saveLevel(previous, dao)
      __ <- saveLevel(next, dao)
      ___ <- dao.linkToPreviousLevel(next.id, previous.id)
    } yield true, 1.second)

  def deleteLevel(db: Database, id: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                DELETE FROM level
                WHERE id = {id};
          """
        ).on(
            'id -> id
          )
          .executeInsert()
      }
    }
}
