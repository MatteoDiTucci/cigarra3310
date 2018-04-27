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
    val uuidGenerator = mock[UuidGenerator]
    when(uuidGenerator.guid).thenReturn("some-guid")

    "receiving description and solution for creating a level" when {

      "it is not the first level created" should {

        "create the level, return its guid and link the level to the previous one" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.save("some-guid", "some-description", "some-solution", "some-cigarra-guid"))
            .thenReturn(Future.successful(false))

          when(levelRepository.findLastCreatedLevelGuid("some-cigarra-guid"))
            .thenReturn(Future.successful(Some("previous-level-guid")))

          when(levelRepository.linkToPreviousLevel("some-guid", "previous-level-guid"))
            .thenReturn(Future.successful(false))

          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.createLevel("some-cigarra-guid", "some-description", "some-solution"), 1.second) mustEqual "some-guid"
          verify(levelRepository, times(1)).save("some-guid", "some-description", "some-solution", "some-cigarra-guid")
          verify(levelRepository, times(1)).findLastCreatedLevelGuid("some-cigarra-guid")
          verify(levelRepository, times(1)).linkToPreviousLevel("some-guid", "previous-level-guid")
          verify(uuidGenerator, times(1)).guid
        }
      }

      "it is the first level created" should {

        "create the level and return its guid" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.save("some-guid", "some-description", "some-solution", "some-cigarra-guid"))
            .thenReturn(Future.successful(false))

          when(levelRepository.findLastCreatedLevelGuid("some-cigarra-guid"))
            .thenReturn(Future.successful(None))

          val service = new LevelService(levelRepository, uuidGenerator)

          Await.result(service.createLevel("some-cigarra-guid", "some-description", "some-solution"), 1.second) mustEqual "some-guid"
          verify(levelRepository, times(1)).save("some-guid", "some-description", "some-solution", "some-cigarra-guid")
          verify(levelRepository, times(1)).findLastCreatedLevelGuid("some-cigarra-guid")
          verify(levelRepository, never()).linkToPreviousLevel("some-guid", "previous-level-guid")
        }
      }
    }

    "receiving a solution for a Level" when {
      val levelRepository = mock[LevelRepository]
      when(levelRepository.find("current-level-guid"))
        .thenReturn(Future.successful(Some(Level("current-level-guid", "some-description", "current-level-solution"))))

      "the solution is correct" when {

        "the solution submitted is identical to the one stored" should {
          val level = Level("current-level-guid", "some-description", "some-solution")
          when(levelRepository.find("current-level-guid"))
            .thenReturn(Future.successful(Some(level)))

          "return true" in {
            val service = new LevelService(levelRepository, uuidGenerator)

            Await.result(service.solveLevel("cigarra-guid", "current-level-guid", "some-solution"), 1.second) mustBe true
          }
        }

        "the solution submitted contains white spaces" should {
          val level = Level("current-level-guid", "some-description", "some-solution")
          when(levelRepository.find("current-level-guid"))
            .thenReturn(Future.successful(Some(level)))

          "return true" in {
            val solutionWithInnerSpaces = "some - solution"
            val solutionWithLeadingSpaces = " some-solution"
            val solutionWithTrailingSpaces = "some-solution "
            val service = new LevelService(levelRepository, uuidGenerator)

            Await.result(service.solveLevel("cigarra-guid", "current-level-guid", solutionWithInnerSpaces), 1.second) mustBe true
            Await.result(service.solveLevel("cigarra-guid", "current-level-guid", solutionWithLeadingSpaces), 1.second) mustBe true
            Await.result(service.solveLevel("cigarra-guid", "current-level-guid", solutionWithTrailingSpaces), 1.second) mustBe true
          }
        }

        "the solution submitted differs for the case" should {
          val level = Level("current-level-guid", "some-description", "some-solution")
          when(levelRepository.find("current-level-guid"))
            .thenReturn(Future.successful(Some(level)))

          "return true" in {
            val service = new LevelService(levelRepository, uuidGenerator)

            Await.result(service.solveLevel("cigarra-guid", "current-level-guid", "some-Solution"), 1.second) mustBe true
          }
        }
      }

      "the solution is not correct" should {

        "return false" in {
          val level = Level("current-level-guid", "some-description", "some-solution")
          when(levelRepository.find("current-level-guid"))
            .thenReturn(Future.successful(Some(level)))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.solveLevel("cigarra-guid", "current-level-guid", "bad-solution"), 1.second)

          result mustBe false
        }
      }

      "the Level cannot be found" should {

        "return false" in {
          when(levelRepository.find("current-level-guid"))
            .thenReturn(Future.successful(None))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.solveLevel("cigarra-guid", "current-level-guid", "bad-solution"), 1.second)

          result mustBe false
        }
      }
    }

    "receiving a Cigarra and a Level guid to retrieve a Level" when {

      "the Level exists" should {

        "return the Level" in {
          val levelRepository = mock[LevelRepository]
          val level = Level("some-level-guid", "some-level-description", "some-level-solution")
          when(levelRepository.find("current-level-guid"))
            .thenReturn(Future.successful(Some(level)))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.findLevel("current-level-guid"), 1.second)

          result mustBe Some(level)
        }
      }

      "the Level does not exist" should {

        "return None" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.find("current-level-guid"))
            .thenReturn(Future.successful(None))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.findLevel("current-level-guid"), 1.second)

          result mustBe None
        }
      }
    }

    "receiving a Level guid to retrieve the next Level" when {

      "the next level exists" should {

        "return the next Level" in {
          val levelRepository = mock[LevelRepository]
          val level = Level("next-level-guid", "next-level-description", "next-level-solution")
          when(levelRepository.findNext("current-level-guid"))
            .thenReturn(Future.successful(Some(level)))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.findNextLevel("current-level-guid"), 1.second)

          result mustBe Some(level)
        }
      }

      "the next level does not exist" should {

        "return None" in {
          val levelRepository = mock[LevelRepository]
          when(levelRepository.findNext("current-level-guid"))
            .thenReturn(Future.successful(None))

          val service = new LevelService(levelRepository, uuidGenerator)

          val result = Await.result(service.findNextLevel("current-level-guid"), 1.second)

          result mustBe None
        }
      }
    }
  }
}
