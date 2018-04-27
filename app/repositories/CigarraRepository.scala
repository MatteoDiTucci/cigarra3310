package repositories

import anorm._
import anorm.SqlParser._
import domain.Cigarra
import javax.inject.{Inject, Named, Singleton}
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CigarraRepository @Inject()(db: Database)(
    @Named("database-execution-context") private implicit val ec: ExecutionContext) {

  def setFirstLevel(cigarraGuid: String, levelGuid: String): Future[Boolean] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                UPDATE cigarra 
                SET first_level_guid = {levelGuid}
                WHERE guid = {cigarraGuid};
          """
        ).on(
            'cigarraGuid -> cigarraGuid,
            'levelGuid -> levelGuid
          )
          .execute()
      }
    }

  def findCigarra(guid: String): Future[Cigarra] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          SELECT guid,name
          FROM cigarra
          WHERE guid = {guid};
        """
      ).on(
          'guid -> guid
        )
        .as(cigarras.single)
    }
  }

  def save(guid: String, name: String): Future[Boolean] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                INSERT INTO cigarra (guid, name, first_level_guid)
                VALUES ({guid}, {name}, NULL);
          """
        ).on(
            'guid -> guid,
            'name -> name
          )
          .execute()
      }
    }

  def findFirstLevel(guid: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT first_level_guid
          FROM cigarra
          WHERE guid = {guid};
        """
        ).on(
            'guid -> guid
          )
          .as(SqlParser.scalar[String].singleOpt)
      }
    }

  private def cigarraLevelCount(guid: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT count(1)
          FROM level
          WHERE cigarra_guid = {guid};
        """
        ).on(
            'guid -> guid
          )
          .as(SqlParser.scalar[Int].single)
      }
    }

  val cigarras: RowParser[Cigarra] =
    str("guid") ~
      str("name") map {
      case guid ~ name =>
        Cigarra(guid, name)
    }

}
