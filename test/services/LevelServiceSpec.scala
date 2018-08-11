package services

import domain.Level
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import repositories.LevelRepository
import org.mockito.Mockito._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LevelServiceSpec extends WordSpec with MockitoSugar with MustMatchers {

  "LevelService" when {
    val uuidGenerator = mock[IdGenerator]
    when(uuidGenerator.id).thenReturn("some-id")

    "receiving description and solution for creating a level" when {

      "it is not the first level created" should {
        val levelRepository = mock[LevelRepository]
        when(levelRepository.findLastCreatedLevelId("some-cigarra-id"))
          .thenReturn(Future.successful(Some("previous-level-id")))

        "create the level, return its id and link the level to the previous one" in {
          when(levelRepository.save("some-id", "some-description", "some-solution", "some-cigarra-id"))
            .thenReturn(Future.successful(false))
          when(levelRepository.linkToPreviousLevel("some-id", "previous-level-id"))
            .thenReturn(Future.successful(false))
          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.createLevel("some-cigarra-id", "some-description", "some-solution"), 1.second) mustEqual "some-id"

          verify(levelRepository, times(1)).save("some-id", "some-description", "some-solution", "some-cigarra-id")
          verify(levelRepository, times(1)).findLastCreatedLevelId("some-cigarra-id")
          verify(levelRepository, times(1)).linkToPreviousLevel("some-id", "previous-level-id")
          verify(uuidGenerator, times(1)).id
        }
      }

      "it is the first level created" should {
        val levelRepository = mock[LevelRepository]
        when(levelRepository.findLastCreatedLevelId("some-cigarra-id"))
          .thenReturn(Future.successful(None))

        "create the level and return its id" in {
          when(levelRepository.save("some-id", "some-description", "some-solution", "some-cigarra-id"))
            .thenReturn(Future.successful(false))
          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.createLevel("some-cigarra-id", "some-description", "some-solution"), 1.second) mustEqual "some-id"

          verify(levelRepository, times(1)).save("some-id", "some-description", "some-solution", "some-cigarra-id")
          verify(levelRepository, times(1)).findLastCreatedLevelId("some-cigarra-id")
          verify(levelRepository, never()).linkToPreviousLevel("some-id", "previous-level-id")
        }
      }
    }

    "receiving a solution for a Level" when {
      val levelRepository = mock[LevelRepository]
      when(levelRepository.find("current-level-id"))
        .thenReturn(Future.successful(Level("current-level-id", "some-description", "current-level-solution")))
      val service = new LevelService(levelRepository, uuidGenerator)

      "the solution is correct" when {
        val level = Level("current-level-id", "some-description", "some-solution")
        when(levelRepository.find("current-level-id"))
          .thenReturn(Future.successful(level))

        "the solution submitted is identical to the one stored" should {

          "return true" in {
            Await.result(service.solveLevel("cigarra-id", "current-level-id", "some-solution"), 1.second) mustBe true
          }
        }

        "the solution submitted contains white spaces" should {
          "return true" in {
            val solutionWithInnerSpaces = "some - solution"
            val solutionWithLeadingSpaces = " some-solution"
            val solutionWithTrailingSpaces = "some-solution "

            Await.result(service.solveLevel("cigarra-id", "current-level-id", solutionWithInnerSpaces), 1.second) mustBe true
            Await.result(service.solveLevel("cigarra-id", "current-level-id", solutionWithLeadingSpaces), 1.second) mustBe true
            Await.result(service.solveLevel("cigarra-id", "current-level-id", solutionWithTrailingSpaces), 1.second) mustBe true
          }
        }

        "the solution submitted differs for the case" should {
          "return true" in {
            Await.result(service.solveLevel("cigarra-id", "current-level-id", "some-Solution"), 1.second) mustBe true
          }
        }
      }

      "the solution is not correct" should {

        "return false" in {
          Await.result(service.solveLevel("cigarra-id", "current-level-id", "bad-solution"), 1.second) mustBe false
        }
      }
    }

    "receiving a Cigarra and a Level id to retrieve a Level" should {
      "return the Level" in {
        val levelRepository = mock[LevelRepository]
        val level = Level("some-level-id", "some-level-description", "some-level-solution")
        when(levelRepository.find("current-level-id"))
          .thenReturn(Future.successful(level))
        val service = new LevelService(levelRepository, uuidGenerator)

        Await.result(service.findLevel("current-level-id"), 1.second) mustBe level
      }
    }

    "receiving a Level id to retrieve the next Level" when {

      "the next level exists" should {

        "return the next Level" in {
          val levelRepository = mock[LevelRepository]
          val level = Level("next-level-id", "next-level-description", "next-level-solution")
          when(levelRepository.findNext("current-level-id"))
            .thenReturn(Future.successful(Some(level)))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.findNextLevel("current-level-id"), 1.second)

          result mustBe Some(level)
        }
      }

      "the next level does not exist" should {

        "return None" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findNext("current-level-id"))
            .thenReturn(Future.successful(None))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.findNextLevel("current-level-id"), 1.second)

          result mustBe None
        }
      }
    }
  }
}
