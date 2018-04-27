package services

import domain.{Cigarra, Level}
import javax.inject.{Inject, Singleton}
import repositories.{CigarraRepository, LevelRepository}

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class CigarraService @Inject()(cigarraRepository: CigarraRepository, levelRepository: LevelRepository)(
    implicit ex: ExecutionContext) {

  def findFirstLevel(cigarraGuid: String): Future[Option[Level]] =
    cigarraRepository.findFirstLevel(cigarraGuid).flatMap {
      case None => Future.successful(None)
      case Some(levelGuid) =>
        levelRepository.find(levelGuid).map(level => Some(level))
    }

  def findCigarra(guid: String): Future[Cigarra] =
    cigarraRepository.findCigarra(guid)

  def createCigarra(cigarraName: String): String = {
    val guid = java.util.UUID.randomUUID.toString
    cigarraRepository.save(guid, cigarraName)
    guid
  }

  def setFirstLevel(cigarraGuid: String, levelGuid: String): Future[Boolean] =
    cigarraRepository.findFirstLevel(cigarraGuid).flatMap {
      case _ @None => cigarraRepository.setFirstLevel(cigarraGuid, levelGuid)
      case Some(_) => Future.successful(true)
    }
}
