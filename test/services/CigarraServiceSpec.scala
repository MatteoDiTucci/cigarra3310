package services

import java.util.UUID

import domain.{Cigarra, Level}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import repositories.{CigarraRepository, LevelRepository}

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class CigarraServiceSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraService" when {

    "creating a new Cigarra" should {

      "return the new Cigarra id" in {
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.save(any[String], any[String])).thenReturn(Future.successful(false))
        val service = createService(cigarraRepository)

        val cigarraId = service.createCigarra("some-name")

        isValidId(cigarraId) mustBe true
        verify(cigarraRepository, times(1)).save(any[String], any[String])
      }
    }

    "retrieving an existing Cigarra by its id" should {

      "return the Cigarra" in {
        val cigarra = Cigarra("some-id", "some-name")
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.findCigarra(any[String])).thenReturn(Future.successful(cigarra))
        val service = createService(cigarraRepository)

        Await.result(service.findCigarra("some-id"), 1.second) mustBe cigarra
      }
    }

    "retrieving the first Level of a Cigarra by its id" should {

      "return the first level of a Cigarra" in {
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.findFirstLevel("some-id")).thenReturn(Future.successful(Some("some-level-id")))

        val level = Level("some-level-id", "some-description", "some-solution")
        val levelRepository = mock[LevelRepository]
        when(levelRepository.find("some-level-id")).thenReturn(Future.successful(level))
        val service = createService(cigarraRepository, levelRepository)

        Await.result(service.findFirstLevel("some-id"), 1.second) mustBe Some(level)
      }
    }

    "setting the Cigarra first Level" when {

      "the Cigarra has no first Level" should {
        "check if the Cigarra does not have a first level and then set it" in {
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findFirstLevel("cigarra-id")).thenReturn(Future.successful(None))
          when(cigarraRepository.setFirstLevel("cigarra-id", "level-id")).thenReturn(Future.successful(false))

          val service = createService(cigarraRepository)
          Await.result(service.setFirstLevel("cigarra-id", "level-id"), 1.second)

          verify(cigarraRepository, times(1)).findFirstLevel("cigarra-id")
          verify(cigarraRepository, times(1)).setFirstLevel("cigarra-id", "level-id")
        }
      }

      "the Cigarra already has a first Level" should {
        "check if the Cigarra does not have a first level" in {
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findFirstLevel("cigarra-id")).thenReturn(Future.successful(Some("first-level-id")))

          val service = createService(cigarraRepository)
          Await.result(service.setFirstLevel("cigarra-id", "level-id"), 1.second)

          verify(cigarraRepository, times(1)).findFirstLevel("cigarra-id")
          verify(cigarraRepository, never()).setFirstLevel("cigarra-id", "level-id")
        }
      }
    }
  }

  private def createService(cigarraRepository: CigarraRepository,
                            levelRepository: LevelRepository = mock[LevelRepository]) =
    new CigarraService(cigarraRepository, levelRepository)

  private def isValidId(id: String) =
    Try(
      UUID.fromString(id)
    ) match {
      case Success(_) => true
      case Failure(_) => false
    }
}
