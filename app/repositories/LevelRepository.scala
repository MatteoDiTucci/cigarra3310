package repositories

import java.util.UUID

import domain.Level

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class LevelRepository {
  val cigarrasLevels: mutable.Map[UUID, ListBuffer[Level]] = mutable.Map()

  def createLevel(cigarraGuid: String, levelWithoutGuid: Level): Option[String] = {
    val level = levelWithoutGuid.copy(guid = Some(java.util.UUID.randomUUID))

    cigarrasLevels.get(UUID.fromString(cigarraGuid)) match {
      case Some(levels: ListBuffer[Level]) => levels += level
      case None                            => cigarrasLevels.put(UUID.fromString(cigarraGuid), ListBuffer(level))
    }
    Some(level.guid.get.toString)
  }
}
