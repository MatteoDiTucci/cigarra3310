package dao

import anorm._
import anorm.SqlParser._
import domain.Cigarra
import javax.inject.{Inject, Named, Singleton}
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CigarraDao @Inject()(db: Database)(
    @Named("database-execution-context") private implicit val ec: ExecutionContext) {

  def setFirstLevel(cigarraId: String, levelId: String): Future[Boolean] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                UPDATE cigarra
                SET first_level_id = {levelId}
                WHERE id = {cigarraId};
          """
        ).on(
            'cigarraId -> cigarraId,
            'levelId -> levelId
          )
          .execute()
      }
    }

  def findCigarra(id: String): Future[Cigarra] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          SELECT id,name
          FROM cigarra
          WHERE id = {id};
        """
      ).on(
          'id -> id
        )
        .as(cigarras.single)
    }
  }

  def save(id: String, name: String): Future[Boolean] =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                INSERT INTO cigarra (id, name, first_level_id)
                VALUES ({id}, {name}, NULL);
          """
        ).on(
            'id -> id,
            'name -> name
          )
          .execute()
      }
    }

  def findFirstLevel(id: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
          SELECT first_level_id
          FROM cigarra
          WHERE id = {id};
        """
        ).on(
            'id -> id
          )
          .as(SqlParser.scalar[String].singleOpt)
      }
    }

  val cigarras: RowParser[Cigarra] =
    str("id") ~
      str("name") map {
      case id ~ name =>
        Cigarra(id, name)
    }

}
