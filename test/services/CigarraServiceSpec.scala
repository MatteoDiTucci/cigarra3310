package services

import java.util.UUID

import domain.Cigarra
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import repositories.CigarraRepository

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

class CigarraServiceSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraService" when {

    "creating a new Cigarra" should {

      "return the new Cigarra guid" in {
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.save(any[String], any[String])).thenReturn(Future.successful(false))
        val service = new CigarraService(cigarraRepository)

        val cigarraGuid = service.createCigarra("some-name")

        isValidGuid(cigarraGuid) mustBe true
        verify(cigarraRepository, times(1)).save(any[String], any[String])
      }
    }

    "retrieving an existing Cigarra by its guid" when {

      "the Cigarra exists" should {

        "return the Cigarra" in {
          val cigarra = Cigarra("some-guid", "some-name")
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findCigarra(any[String])).thenReturn(Future.successful(Some(cigarra)))
          val service = new CigarraService(cigarraRepository)

          val result = Await.result(service.findCigarra("some-guid"), 1.second)

          result mustBe Some(cigarra)
        }
      }

      "CigarraRepository is not able to find a Cigarra" should {

        "return a None" in {
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findCigarra(any[String])).thenReturn(Future.successful(None))
          val service = new CigarraService(cigarraRepository)

          val result = Await.result(service.findCigarra("some-guid"), 1.second)

          result mustBe None
        }
      }
    }
  }

  private def isValidGuid(guid: String) =
    Try(
      UUID.fromString(guid)
    ) match {
      case Success(_) => true
      case Failure(_) => false
    }
}
