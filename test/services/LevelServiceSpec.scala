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

  private val cigarraId = "some-cigarra-id"
  private val previousLevelId = "previous-level-id"
  private val levelId = "some-level-id"
  private val level = Level(levelId, "some-description", "some-solution")

  "LevelService" when {
    val uuidGenerator = mock[IdGenerator]
    when(uuidGenerator.id).thenReturn(levelId)

    "receiving description and solution for creating a level" when {

      "a previous level exist for the same cigarra" should {
        val levelRepository = mock[LevelRepository]
        when(levelRepository.findLastCreatedLevelId(cigarraId))
          .thenReturn(Future.successful(Some(previousLevelId)))

        "create a new level, return its id and link the previous level to it" in {
          mockSuccessfulSaveLevel(level, levelRepository)
          mockSuccessfulLinkToPreviousLevel(levelRepository)
          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.createLevel(cigarraId, "some-description", "some-solution"), 1.second) mustEqual levelId

          verify(levelRepository, times(1)).save(cigarraId, level)
          verify(levelRepository, times(1)).findLastCreatedLevelId(cigarraId)
          verify(levelRepository, times(1)).linkToPreviousLevel(levelId, previousLevelId)
          verify(uuidGenerator, times(1)).id
        }
      }

      "it is the first level created for the cigarra" should {
        val levelRepository = mock[LevelRepository]
        mockNoPreviousLevelCreated(levelRepository)

        "create the level and return its id" in {
          mockSuccessfulSaveLevel(level, levelRepository)
          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.createLevel(cigarraId, "some-description", "some-solution"), 1.second) mustEqual levelId

          verify(levelRepository, times(1)).save(cigarraId, level)
          verify(levelRepository, times(1)).findLastCreatedLevelId(cigarraId)
          verify(levelRepository, never()).linkToPreviousLevel(levelId, previousLevelId)
        }
      }
    }

    "receiving a solution for a Level" when {
      val levelRepository = mock[LevelRepository]
      mockLevelExistsWithSolution("some-solution", levelRepository)

      val service = new LevelService(levelRepository, uuidGenerator)

      "the solution is correct" should {

        "return true" in {
          Await.result(service.solveLevel(cigarraId, levelId, "some-solution"), 1.second) mustBe true
        }

        "the solution submitted contains white spaces" should {
          "return true" in {
            val solutionWithInnerSpaces = "some - solution"
            val solutionWithLeadingSpaces = " some-solution"
            val solutionWithTrailingSpaces = "some-solution "

            Await.result(service.solveLevel(cigarraId, levelId, solutionWithInnerSpaces), 1.second) mustBe true
            Await.result(service.solveLevel(cigarraId, levelId, solutionWithLeadingSpaces), 1.second) mustBe true
            Await.result(service.solveLevel(cigarraId, levelId, solutionWithTrailingSpaces), 1.second) mustBe true
          }
        }

        "the solution submitted differs for the case" should {
          "return true" in {
            Await.result(service.solveLevel(cigarraId, levelId, "some-Solution"), 1.second) mustBe true
          }
        }
      }

      "the solution is not correct" should {

        "return false" in {
          Await.result(service.solveLevel(cigarraId, levelId, "bad-solution"), 1.second) mustBe false
        }
      }
    }

    "receiving a cigarra and a Level id to retrieve a Level" should {
      "return the Level" in {
        val levelRepository = mock[LevelRepository]
        when(levelRepository.find(levelId)).thenReturn(Future.successful(level))
        val service = new LevelService(levelRepository, uuidGenerator)

        Await.result(service.findLevel(levelId), 1.second) mustBe level
      }
    }

    "receiving a Level id to retrieve the next Level" when {

      "the next level exists" should {

        "return the next Level" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findNext(levelId))
            .thenReturn(Future.successful(Some(level)))

          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.findNextLevel(levelId), 1.second) mustBe Some(level)
        }
      }

      "the next level does not exist" should {

        "return None" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findNext(levelId))
            .thenReturn(Future.successful(None))

          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.findNextLevel(levelId), 1.second) mustBe None
        }
      }
    }
  }
  private def mockLevelExistsWithSolution(solution: String, levelRepository: LevelRepository) =
    when(levelRepository.find(levelId))
      .thenReturn(Future.successful(Level(levelId, "some-description", solution)))

  private def mockNoPreviousLevelCreated(levelRepository: LevelRepository) =
    when(levelRepository.findLastCreatedLevelId(cigarraId))
      .thenReturn(Future.successful(None))

  private def mockSuccessfulLinkToPreviousLevel(levelRepository: LevelRepository) =
    when(levelRepository.linkToPreviousLevel(levelId, previousLevelId)).thenReturn(Future.successful(true))

  private def mockSuccessfulSaveLevel(level: Level, levelRepository: LevelRepository) =
    when(levelRepository.save(cigarraId, level))
      .thenReturn(Future.successful(true))
}
