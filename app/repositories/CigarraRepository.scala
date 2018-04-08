package repositories

import java.util.UUID

import domain.Cigarra
import javax.inject.{Inject, Singleton}

import scala.collection.mutable

@Singleton
class CigarraRepository @Inject()() {
  private val cigarras: mutable.Map[UUID, Cigarra] = mutable.Map()

  def save(cigarra: Cigarra): Option[String] = {
    val guid: UUID = java.util.UUID.randomUUID
    cigarras.put(guid, cigarra)
    Some(guid.toString)
  }
}
