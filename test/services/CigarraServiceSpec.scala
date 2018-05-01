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

      "return the new Cigarra guid" in {
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.save(any[String], any[String])).thenReturn(Future.successful(false))
        val service = createService(cigarraRepository)

        val cigarraGuid = service.createCigarra("some-name")

        isValidGuid(cigarraGuid) mustBe true
        verify(cigarraRepository, times(1)).save(any[String], any[String])
      }
    }

    "retrieving an existing Cigarra by its guid" should {

      "return the Cigarra" in {
        val cigarra = Cigarra("some-guid", "some-name")
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.findCigarra(any[String])).thenReturn(Future.successful(cigarra))
        val service = createService(cigarraRepository)

        Await.result(service.findCigarra("some-guid"), 1.second) mustBe cigarra
      }
    }

    "retrieving the first Level of a Cigarra by its guid" should {

      "return the first level of a Cigarra" in {
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.findFirstLevel("some-guid")).thenReturn(Future.successful(Some("some-level-guid")))

        val level = Level("some-level-guid", "some-description", "some-solution")
        val levelRepository = mock[LevelRepository]
        when(levelRepository.find("some-level-guid")).thenReturn(Future.successful(level))
        val service = createService(cigarraRepository, levelRepository)

        Await.result(service.findFirstLevel("some-guid"), 1.second) mustBe Some(level)
      }
    }

    "setting the Cigarra first Level" when {

      "the Cigarra has no first Level" should {
        "check if the Cigarra does not have a first level and then set it" in {
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findFirstLevel("cigarra-guid")).thenReturn(Future.successful(None))
          when(cigarraRepository.setFirstLevel("cigarra-guid", "level-guid")).thenReturn(Future.successful(false))

          val service = createService(cigarraRepository)
          Await.result(service.setFirstLevel("cigarra-guid", "level-guid"), 1.second)

          verify(cigarraRepository, times(1)).findFirstLevel("cigarra-guid")
          verify(cigarraRepository, times(1)).setFirstLevel("cigarra-guid", "level-guid")
        }
      }

      "the Cigarra already has a first Level" should {
        "check if the Cigarra does not have a first level" in {
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findFirstLevel("cigarra-guid")).thenReturn(Future.successful(Some("first-level-guid")))

          val service = createService(cigarraRepository)
          Await.result(service.setFirstLevel("cigarra-guid", "level-guid"), 1.second)

          verify(cigarraRepository, times(1)).findFirstLevel("cigarra-guid")
          verify(cigarraRepository, never()).setFirstLevel("cigarra-guid", "level-guid")
        }
      }
    }
  }

  private def createService(cigarraRepository: CigarraRepository,
                            levelRepository: LevelRepository = mock[LevelRepository]) =
    new CigarraService(cigarraRepository, levelRepository)

  private def isValidGuid(guid: String) =
    Try(
      UUID.fromString(guid)
    ) match {
      case Success(_) => true
      case Failure(_) => false
    }
}
