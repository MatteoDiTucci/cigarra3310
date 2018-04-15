package services

import domain.Cigarra
import javax.inject.{Inject, Singleton}
import repositories.CigarraRepository

@Singleton
class CigarraService @Inject()(cigarraRepository: CigarraRepository) {
  def createCigarra(name: String): Option[String] = cigarraRepository.save(Cigarra(name = name))

  def findCigarra(guid: String): Option[Cigarra] = cigarraRepository.findCigarra(guid)
}
