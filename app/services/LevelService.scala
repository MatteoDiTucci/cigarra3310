package services

import domain.Level
import javax.inject.{Inject, Singleton}
import repositories.LevelRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LevelService @Inject()(levelRepository: LevelRepository, uuidGenerator: IdGenerator)(
    implicit ex: ExecutionContext) {

  def solveLevel(cigarraId: String, currentLevelId: String, submittedSolution: String): Future[Boolean] =
    levelRepository
      .find(currentLevelId)
      .map { level =>
        solve(level, submittedSolution)
      }

  private def solve(level: Level, solution: String): Boolean = {
    val sanitizedSolution = solution.toLowerCase.replaceAll("\\s", "")
    val sanitizedLevelSolution = level.solution.toLowerCase.replaceAll("\\s", "")
    sanitizedLevelSolution.equals(sanitizedSolution)
  }

  def createLevel(cigarraId: String, description: String, solution: String): Future[String] = {
    val currentLevelId = uuidGenerator.id
    levelRepository.findLastCreatedLevelId(cigarraId).flatMap {
      case Some(previousLevelId) =>
        levelRepository.linkToPreviousLevel(currentLevelId, previousLevelId)
        saveLevel(cigarraId, description, solution, currentLevelId)
      case None => saveLevel(cigarraId, description, solution, currentLevelId)
    }
  }

  private def saveLevel(cigarraId: String, description: String, solution: String, levelId: String) =
    levelRepository
      .save(levelId = levelId, description = description, solution = solution, cigarraId = cigarraId)
      .map { _ =>
        levelId
      }

  def findLevel(levelId: String): Future[Level] = levelRepository.find(levelId)

  def findNextLevel(levelId: String): Future[Option[Level]] = levelRepository.findNext(levelId)
}
