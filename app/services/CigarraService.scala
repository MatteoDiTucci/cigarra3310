package services

import domain.Cigarra
import javax.inject.{Inject, Singleton}
import repositories.CigarraRepository

import scala.concurrent.Future
@Singleton
class CigarraService @Inject()(cigarraRepository: CigarraRepository) {

  def findCigarra(guid: String): Future[Option[Cigarra]] =
    cigarraRepository.findCigarra(guid)

  def createCigarra(cigarraName: String): String = {
    val guid = java.util.UUID.randomUUID.toString
    cigarraRepository.save(guid, cigarraName)
    guid
  }
}
