package services

import domain.{Cigarra, Level}
import javax.inject.{Inject, Singleton}
import repositories.{CigarraRepository, LevelRepository}

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class CigarraService @Inject()(cigarraRepository: CigarraRepository, levelRepository: LevelRepository)(
    implicit ex: ExecutionContext) {

  def findFirstLevel(cigarraId: String): Future[Option[Level]] =
    cigarraRepository.findFirstLevel(cigarraId).flatMap {
      case None => Future.successful(None)
      case Some(levelId) =>
        levelRepository.find(levelId).map(level => Some(level))
    }

  def findCigarra(id: String): Future[Cigarra] =
    cigarraRepository.findCigarra(id)

  def createCigarra(cigarraName: String): String = {
    val id = java.util.UUID.randomUUID.toString
    cigarraRepository.save(id, cigarraName)
    id
  }

  def setFirstLevel(cigarraId: String, levelId: String): Future[Boolean] =
    cigarraRepository.findFirstLevel(cigarraId).flatMap {
      case _ @None => cigarraRepository.setFirstLevel(cigarraId, levelId)
      case Some(_) => Future.successful(true)
    }
}
