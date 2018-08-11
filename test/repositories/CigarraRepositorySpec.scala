package repositories

import anorm.SQL
import domain.{Cigarra, Level}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import play.api.db.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CigarraRepositorySpec extends WordSpec with MustMatchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteCigarra(database, "some-guid"), 1.second)
  }

  override def afterEach(): Unit = DbFixtures.withMyDatabase { database =>
    Await.result(deleteCigarra(database, "some-guid"), 1.second)
  }

  "CigarraRepository" when {

    "saving the Cigarra" should {

      "persist it in DB" in {
        DbFixtures.withMyDatabase { database =>
          val cigarra = Cigarra("some-guid", "some-name")
          val cigarraRepository = new CigarraRepository(database)

          Await.result(cigarraRepository.save("some-guid", "some-name"), 1.second)

          Await.result(cigarraRepository.findCigarra("some-guid"), 1.second) mustEqual cigarra
        }
      }
    }

    "retrieving the first Level guid of a Cigarra by its guid" when {

      "the first level exists" should {

        "return the first Level guid" in {
          DbFixtures.withMyDatabase { database =>
            val levelId = "first-level-guid"
            val cigarraRepository = new CigarraRepository(database)
            Await.result(saveCigarraWithFirstLevel(database, "some-guid", "cigarra-name", "first-level-guid"), 1.second)

            Await.result(cigarraRepository.findFirstLevel("some-guid"), 1.second) mustBe Some(levelId)
          }
        }
      }

      "the first Level does not exist" should {

        "return None" in {
          DbFixtures.withMyDatabase { database =>
            val cigarraRepository = new CigarraRepository(database)
            Await.result(cigarraRepository.save("some-guid", "cigarra-name"), 1.second)

            Await.result(cigarraRepository.findFirstLevel("some-guid"), 1.second) mustBe None
          }
        }
      }
    }

    "setting Cigarra first level" should {

      "update Cigarra with its first level" in {
        DbFixtures.withMyDatabase { database =>
          val cigarraRepository = new CigarraRepository(database)
          Await.result(cigarraRepository.save("some-guid", "some-name"), 1.second)
          Await.result(cigarraRepository.setFirstLevel("some-guid", "level-guid"), 1.second)

          Await.result(cigarraRepository.findFirstLevel("some-guid"), 1.second) mustBe Some("level-guid")
        }
      }
    }
  }

  private def deleteCigarra(db: Database, guid: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                DELETE FROM cigarra
                WHERE guid = {guid};
          """
        ).on(
            'guid -> guid
          )
          .executeInsert()
      }
    }

  private def saveCigarraWithFirstLevel(db: Database, guid: String, name: String, firstLevelGuid: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                INSERT INTO cigarra (guid, name, first_level_guid)
                VALUES ({guid}, {name}, {nextLevelGuid});
          """
        ).on(
            'guid -> guid,
            'name -> name,
            'nextLevelGuid -> firstLevelGuid
          )
          .execute()
      }
    }
}
