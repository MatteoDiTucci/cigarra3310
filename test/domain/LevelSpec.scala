package domain

import org.scalatest.{MustMatchers, WordSpec}

class LevelSpec extends WordSpec with MustMatchers {
  "Level object" when {

    "provided with a Level and a solution to solve it" when {

      "the solution is correct but contains white spaces" should {

        "return true" in {
          val level = Level(Some("guid"), "some-description", "some-solution")
          val solutionWithInnerSpaces = "some - solution"
          val solutionWithLeadingSpaces = " some-solution"
          val solutionWithTrailingSpaces = "some-solution "

          Level.solve(level, solutionWithInnerSpaces) mustBe true
          Level.solve(level, solutionWithLeadingSpaces) mustBe true
          Level.solve(level, solutionWithTrailingSpaces) mustBe true
        }
      }

      "the solution is correct but differs for the case" should {

        "return true" in {
          val level = Level(Some("guid"), "some-description", "some-solution")
          val solution = "some-Solution"

          Level.solve(level, solution) mustBe true
        }
      }

      "the solution is correct" should {

        "return true" in {
          val level = Level(Some("guid"), "some-description", "some-solution")
          val solution = "some-solution"

          Level.solve(level, solution) mustBe true
        }
      }

      "the solution is not correct" should {

        "return false" in {
          val level = Level(Some("guid"), "some-description", "some-solution")
          val solution = "bad-solution"

          Level.solve(level, solution) mustBe false
        }
      }
    }
  }
}
