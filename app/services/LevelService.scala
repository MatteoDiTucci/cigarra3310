package services

import domain.Level
import javax.inject.{Inject, Singleton}
import repositories.LevelRepository

@Singleton
class LevelService @Inject()(levelRepository: LevelRepository) {
  def solveLevel(cigarraGuid: String, currentLevelGuid: String, submittedSolution: String): Option[Boolean] =
    for {
      level <- levelRepository.findLevel(cigarraGuid, currentLevelGuid)
      isSolved = level.solution.equals(submittedSolution)
    } yield isSolved

  def createLevel(cigarraGuid: String, description: String, solution: String): Option[String] =
    levelRepository.createLevel(cigarraGuid = cigarraGuid,
                                description = description,
                                solution = solution,
                                levelGuid = java.util.UUID.randomUUID.toString)

  def findLevel(cigarraGuid: String, levelGuid: String): Option[Level] =
    levelRepository.findLevel(cigarraGuid, levelGuid)

  def findFirstLevel(cigarraGuid: String): Option[Level] = levelRepository.findFirstLevel(cigarraGuid)

  def findNextLevel(cigarraGuid: String, levelGuid: String): Option[Level] =
    levelRepository.findNextLevel(cigarraGuid, levelGuid)
}
