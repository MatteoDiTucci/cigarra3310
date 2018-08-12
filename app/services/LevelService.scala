package services

import domain.Level
import javax.inject.{Inject, Singleton}
import dao.LevelDao

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LevelService @Inject()(levelRepository: LevelDao, idGenerator: IdGenerator)(implicit ex: ExecutionContext) {

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
    val level = createLevel(description, solution)
    for {
      __ <- saveLevel(cigarraId, level)
      _ <- linkLevelWithLastOne(cigarraId, level)

    } yield level.id
  }

  private def createLevel(description: String, solution: String): Level = {
    val id = idGenerator.id
    Level(id, description, solution)
  }

  private def linkLevelWithLastOne(cigarraId: String, level: Level): Future[Boolean] =
    for {
      lastCreatedLevelId: Option[String] <- levelRepository.findLastCreatedLevelId(cigarraId)
      __ <- lastCreatedLevelId.fold(Future.successful(true))(levelRepository.linkToPreviousLevel(level.id, _))
    } yield true

  private def saveLevel(cigarraId: String, level: Level) =
    levelRepository
      .save(cigarraId, level)
      .map { _ =>
        level.id
      }

  def findLevel(levelId: String): Future[Level] = levelRepository.find(levelId)

  def findNextLevel(levelId: String): Future[Option[Level]] = levelRepository.findNext(levelId)
}
