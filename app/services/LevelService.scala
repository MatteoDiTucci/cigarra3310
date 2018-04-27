package services

import domain.Level
import javax.inject.{Inject, Singleton}
import repositories.LevelRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LevelService @Inject()(levelRepository: LevelRepository, uuidGenerator: UuidGenerator)(
    implicit ex: ExecutionContext) {

  def solveLevel(cigarraGuid: String, currentLevelGuid: String, submittedSolution: String): Future[Boolean] =
    levelRepository
      .find(currentLevelGuid)
      .map { maybeLevel =>
        maybeLevel.fold(false)(level => solve(level, submittedSolution))
      }

  private def solve(level: Level, solution: String): Boolean = {
    val sanitizedSolution = solution.toLowerCase.replaceAll("\\s", "")
    val sanitizedLevelSolution = level.solution.toLowerCase.replaceAll("\\s", "")
    sanitizedLevelSolution.equals(sanitizedSolution)
  }

  def createLevel(cigarraGuid: String, description: String, solution: String): Future[String] = {
    val currentLevelGuid = uuidGenerator.guid
    levelRepository.findLastCreatedLevelGuid(cigarraGuid).flatMap {
      case Some(previousLevelGuid) =>
        levelRepository.linkToPreviousLevel(currentLevelGuid, previousLevelGuid)
        saveLevel(cigarraGuid, description, solution, currentLevelGuid)
      case None => saveLevel(cigarraGuid, description, solution, currentLevelGuid)
    }
  }

  private def saveLevel(cigarraGuid: String, description: String, solution: String, levelGuid: String) =
    levelRepository
      .save(levelGuid = levelGuid, description = description, solution = solution, cigarraGuid = cigarraGuid)
      .map { _ =>
        levelGuid
      }

  def findLevel(levelGuid: String): Future[Option[Level]] = levelRepository.find(levelGuid)

  def findNextLevel(levelGuid: String): Future[Option[Level]] = levelRepository.findNext(levelGuid)
}
