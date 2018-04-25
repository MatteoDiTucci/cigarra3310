package repositories

import java.util.UUID

import domain.Level

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class LevelRepository {
  val cigarrasLevels: mutable.Map[UUID, ListBuffer[Level]] = mutable.Map()

  def findNextLevel(cigarraGuid: String, levelGuid: String): Option[Level] =
    for {
      cigarraLevels <- cigarrasLevels.get(UUID.fromString(cigarraGuid))
      level <- getNextLevelFromCigarra(cigarraLevels, levelGuid)
    } yield level

  def findLevel(cigarraGuid: String, levelGuid: String): Option[Level] =
    for {
      cigarraLevels <- cigarrasLevels.get(UUID.fromString(cigarraGuid))
      level <- getLevelFromCigarra(cigarraLevels, levelGuid)
    } yield level

  def createLevel(cigarraGuid: String, description: String, solution: String, levelGuid: String): Option[String] = {
    val level = Level(levelGuid, description, solution)
    cigarrasLevels.get(UUID.fromString(cigarraGuid)) match {
      case Some(levels: ListBuffer[Level]) => levels += level
      case None                            => cigarrasLevels.put(UUID.fromString(cigarraGuid), ListBuffer(level))
    }
    Some(level.guid)
  }
  def findFirstLevel(cigarraGuid: String): Option[Level] =
    cigarrasLevels.get(UUID.fromString(cigarraGuid)) match {
      case Some(levels) => Some(levels.head)
      case None         => None
    }

  private def getLevelFromCigarra(levels: ListBuffer[Level], levelGuid: String): Option[Level] =
    levels.collectFirst { case level if level.guid.equals(levelGuid) => level }

  private def getNextLevelFromCigarra(levels: ListBuffer[Level], levelGuid: String): Option[Level] = {
    val iterator: Iterator[Level] = levels.iterator
    val maybeFoundLevel = findLevel(iterator, levelGuid)

    maybeFoundLevel.flatMap(_ => if (iterator.hasNext) Some(iterator.next()) else None)
  }

  private def findLevel(iterator: Iterator[Level], guid: String): Option[Level] = {
    while (iterator.hasNext) {
      val level = iterator.next()
      if (level.guid.equals(guid)) {
        return Some(level)
      }
    }
    None
  }
}
