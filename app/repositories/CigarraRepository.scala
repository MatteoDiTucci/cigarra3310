package repositories

import java.util.UUID

import anorm._
import anorm.SqlParser._
import domain.Cigarra
import javax.inject.{Inject, Named, Singleton}
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CigarraRepository @Inject()(db: Database)(
    @Named("database-execution-context") private implicit val ec: ExecutionContext) {

  def findCigarra(guid: String): Future[Option[Cigarra]] = Future {
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
        .as(cigarras.singleOpt)
    }
  }

  def save(guid: String, name: String) =
    Future {
      db.withConnection { implicit connection =>
        SQL(
          """
                INSERT INTO cigarra
                VALUES ({guid}, {name});
          """
        ).on(
            'guid -> guid,
            'name -> name
          )
          .execute()
      }
    }

  val cigarras: RowParser[Cigarra] =
    str("guid") ~
      str("name") map {
      case guid ~ name =>
        Cigarra(guid, name)
    }

}
