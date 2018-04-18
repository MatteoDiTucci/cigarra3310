package domain

case class Level(guid: Option[String] = None, description: String, solution: String)

object Level {
  def solve(level: Level, solution: String): Boolean = {
    val sanitizedSolution = solution.toLowerCase.replaceAll("\\s", "")
    val sanitizedLevelSolution = level.solution.toLowerCase.replaceAll("\\s", "")
    sanitizedLevelSolution.equals(sanitizedSolution)
  }
}
