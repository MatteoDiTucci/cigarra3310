package repositories

import anorm.SqlParser.str
import anorm.{~, RowParser, SQL, SqlParser}
import domain.Level
import javax.inject.{Inject, Named, Singleton}
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LevelRepository @Inject()(db: Database)(
    @Named("database-execution-context") private implicit val ec: ExecutionContext) {

  val level: RowParser[Level] =
    str("guid") ~
      str("description") ~
      str("solution") map {
      case guid ~ description ~ solution =>
        Level(guid, description, solution)
    }

  def findNext(guid: String): Future[Option[Level]] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT guid, description, solution
          FROM level
          WHERE guid = (
                       SELECT next_level_guid
                       FROM level
                       WHERE guid = {guid});
        """
        ).on(
            'guid -> guid
          )
          .as(level.singleOpt)
      }
    }

  def findLastCreatedLevelGuid(cigarraGuid: String): Future[Option[String]] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT guid
          FROM level
          WHERE cigarra_guid = {cigarraGuid} AND next_level_guid is NULL;
        """
        ).on(
            'cigarraGuid -> cigarraGuid
          )
          .as(SqlParser.scalar[String].singleOpt)
      }
    }

  def find(levelGuid: String): Future[Level] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT guid, description, solution
          FROM level
          WHERE guid = {guid};
        """
        ).on(
            'guid -> levelGuid
          )
          .as(level.single)
      }
    }

  def save(levelGuid: String, description: String, solution: String, cigarraGuid: String): Future[Boolean] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                INSERT INTO level (guid, next_level_guid ,cigarra_guid, description, solution)
                VALUES ({guid}, NULL, {cigarraGuid}, {description}, {solution});
          """
        ).on(
            'guid -> levelGuid,
            'cigarraGuid -> cigarraGuid,
            'description -> description,
            'solution -> solution
          )
          .execute()
      }
    }

  def linkToPreviousLevel(levelGuid: String, previousLevelGuid: String): Future[Boolean] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                UPDATE level 
                SET next_level_guid = {levelGuid}
                WHERE guid = {previousLevelGuid};
          """
        ).on(
            'levelGuid -> levelGuid,
            'previousLevelGuid -> previousLevelGuid
          )
          .execute()
      }
    }
}
