package repositories

import java.util.UUID

import domain.Cigarra
import javax.inject.{Inject, Singleton}

import scala.collection.mutable

@Singleton
class CigarraRepository @Inject()() {
  val cigarras: mutable.Map[UUID, Cigarra] = mutable.Map()

  def findCigarra(guid: String): Option[Cigarra] = cigarras.get(UUID.fromString(guid))

  def save(cigarraWithoutGuid: Cigarra): Option[String] = {
    val guid: UUID = java.util.UUID.randomUUID
    val cigarra = cigarraWithoutGuid.copy(guid = Some(guid.toString))
    cigarras.put(guid, cigarra)
    Some(guid.toString)
  }
}
