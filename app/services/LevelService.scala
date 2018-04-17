package services

import domain.Level
import javax.inject.{Inject, Singleton}
import repositories.LevelRepository

@Singleton
class LevelService @Inject()(levelRepository: LevelRepository) {
  def findLevel(cigarraGuid: String, levelGuid: String): Option[Level] =
    levelRepository.findLevel(cigarraGuid, levelGuid)

  def solveLevel(cigarraGuid: String, currentLevelGuid: String, submittedSolution: String): Option[Level] =
    for {
      level <- levelRepository.findLevel(cigarraGuid, currentLevelGuid)
      isSolutionCorrect = level.solution.equals(submittedSolution)
      nextLevel <- levelRepository.findNextLevel(cigarraGuid, currentLevelGuid) if isSolutionCorrect
    } yield nextLevel

  def createLevel(cigarraGuid: String, description: String, solution: String): Option[String] =
    levelRepository.createLevel(cigarraGuid, Level(description = description, solution = solution))

  def findFirstLevel(cigarraGuid: String): Option[Level] = levelRepository.findFirstLevel(cigarraGuid)
}
