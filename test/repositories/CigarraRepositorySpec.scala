package repositories

import java.util.UUID

import anorm.SQL
import com.typesafe.config.{Config, ConfigFactory}
import domain.Cigarra
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import play.api.db.{Database, Databases}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class CigarraRepositorySpec extends WordSpec with MustMatchers with BeforeAndAfterEach {
  val config: Config = ConfigFactory.load("test.conf")

  override def afterEach(): Unit = withMyDatabase { database =>
    Await.result(deleteCigarra(database, "some-guid"), 1.second)
  }

  "CigarraRepository" when {

    "persist the Cigarra" in {
      withMyDatabase { database =>
        val cigarraRepository = new CigarraRepository(database)

        Await.result(cigarraRepository.save("some-guid", "some-name"), 1.second)

        Await.result(cigarraRepository.findCigarra("some-guid"), 1.second) mustBe defined
      }
    }

    "retrieving an existing Cigarra by its guid" when {

      "the Cigarra exists" should {

        "return the related Cigarra" in {
          withMyDatabase { database =>
            val cigarraRepository = new CigarraRepository(database)
            Await.result(cigarraRepository.save("some-guid", "some-name"), 1.second)

            val cigarra = Await.result(cigarraRepository.findCigarra("some-guid"), 1.second)

            cigarra mustBe defined
          }
        }
      }

      "the Cigarra cannot be found" should {

        "return None" in {
          withMyDatabase { database =>
            val cigarraRepository = new CigarraRepository(database)

            val cigarra = Await.result(cigarraRepository.findCigarra("some-guid"), 1.second)

            cigarra mustBe None
          }
        }
      }
    }
  }

  def deleteCigarra(db: Database, guid: String) =
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

  def withMyDatabase[T](block: Database => T): T =
    Databases.withDatabase(
      driver = config.getString("db.default.driver"),
      url = config.getString("db.default.url"),
      config = Map()
    )(block)
}
