package services

import domain.Level
import javax.inject.{Inject, Singleton}
import repositories.LevelRepository

@Singleton
class LevelService @Inject()(levelRepository: LevelRepository) {
  def createLevel(cigarraGuid: String, description: String, solution: String): Option[String] =
    levelRepository.createLevel(cigarraGuid, Level(description = description, solution = solution))

  def findFirstLevel(cigarraGuid: String): Option[Level] = levelRepository.findFirstLevel(cigarraGuid)
}
