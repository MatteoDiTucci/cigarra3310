package dao

import anorm.SqlParser.str
import anorm.{~, RowParser, SQL, SqlParser}
import domain.Level
import javax.inject.{Inject, Named, Singleton}
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LevelDao @Inject()(db: Database)(@Named("database-execution-context") private implicit val ec: ExecutionContext) {

  val level: RowParser[Level] =
    str("id") ~
      str("description") ~
      str("solution") map {
      case id ~ description ~ solution =>
        Level(id, description, solution)
    }

  def findNext(id: String): Future[Option[Level]] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT id, description, solution
          FROM level
          WHERE id = (
                       SELECT next_level_id
                       FROM level
                       WHERE id = {id});
        """
        ).on(
            'id -> id
          )
          .as(level.singleOpt)
      }
    }

  def findLastCreatedLevelId(cigarraId: String): Future[Option[String]] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT id
          FROM level
          WHERE cigarra_id = {cigarraId} AND next_level_id is NULL;
        """
        ).on(
            'cigarraId -> cigarraId
          )
          .as(SqlParser.scalar[String].singleOpt)
      }
    }

  def find(levelId: String): Future[Level] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT id, description, solution
          FROM level
          WHERE id = {id};
        """
        ).on(
            'id -> levelId
          )
          .as(level.single)
      }
    }

  def save(cigarraId: String, level: Level) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                INSERT INTO level (id, next_level_id ,cigarra_id, description, solution)
                VALUES ({id}, NULL, {cigarraId}, {description}, {solution});
          """
        ).on(
            'id -> level.id,
            'cigarraId -> cigarraId,
            'description -> level.description,
            'solution -> level.solution
          )
          .execute()
      }
    }

  def linkToPreviousLevel(currentLevelId: String, previousLevelId: String): Future[Boolean] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                UPDATE level
                SET next_level_id = {levelId}
                WHERE id = {previousLevelId};
          """
        ).on(
            'levelId -> currentLevelId,
            'previousLevelId -> previousLevelId
          )
          .execute()
      }
    }
}
