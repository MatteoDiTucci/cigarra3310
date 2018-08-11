package repositories

import anorm.SQL
import domain.Cigarra
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import play.api.db.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CigarraRepositorySpec extends WordSpec with MustMatchers with BeforeAndAfterEach {

  private val cigarraId = "some-cigarra-id"

  override def beforeEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteCigarra(database, cigarraId), 1.second)
  }

  override def afterEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteCigarra(database, cigarraId), 1.second)
  }

  "CigarraRepository" when {

    "saving the Cigarra" should {

      "persist it in DB" in {
        DbFixtures.withMyDatabase { database =>
          val cigarraRepository = new CigarraRepository(database)
          val cigarra = Cigarra(cigarraId, "some-name")

          Await.result(cigarraRepository.save(cigarraId, "some-name"), 1.second)

          Await.result(cigarraRepository.findCigarra(cigarraId), 1.second) mustEqual cigarra
        }
      }
    }

    "retrieving the first level id of a Cigarra by the cigarra id" when {

      "the first level exists" should {

        "return the first level id" in {
          DbFixtures.withMyDatabase { database =>
            val levelId = "first-level-id"
            val cigarraRepository = new CigarraRepository(database)
            Await.result(saveCigarraWithFirstLevel(database, cigarraId, levelId), 1.second)

            Await.result(cigarraRepository.findFirstLevel(cigarraId), 1.second) mustBe Some(levelId)
          }
        }
      }

      "the first Level does not exist" should {

        "return nothing" in {
          DbFixtures.withMyDatabase { database =>
            val cigarraRepository = new CigarraRepository(database)
            Await.result(cigarraRepository.save(cigarraId, "some-cigarra-name"), 1.second)

            Await.result(cigarraRepository.findFirstLevel(cigarraId), 1.second) mustBe None
          }
        }
      }
    }

    "setting cigarra first level" should {

      "update cigarra with its first level" in {
        DbFixtures.withMyDatabase { database =>
          val cigarraRepository = new CigarraRepository(database)
          val levelId = "some-level-id"
          Await.result(cigarraRepository.save(cigarraId, "some-name"), 1.second)
          Await.result(cigarraRepository.setFirstLevel(cigarraId, levelId), 1.second)

          Await.result(cigarraRepository.findFirstLevel(cigarraId), 1.second) mustBe Some(levelId)
        }
      }
    }
  }

  private def deleteCigarra(db: Database, id: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                DELETE FROM cigarra
                WHERE id = {id};
          """
        ).on(
            'id -> id
          )
          .executeInsert()
      }
    }

  private def saveCigarraWithFirstLevel(db: Database, cigarraId: String, firstLevelId: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                INSERT INTO cigarra (id, name, first_level_id)
                VALUES ({id}, {name}, {nextLevelId});
          """
        ).on(
            'id -> cigarraId,
            'name -> "some-name",
            'nextLevelId -> firstLevelId
          )
          .execute()
      }
    }
}
