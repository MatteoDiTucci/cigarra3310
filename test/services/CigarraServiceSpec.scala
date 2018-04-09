package services

import domain.Cigarra
import org.scalatest.{MustMatchers, WordSpec}
import repositories.CigarraRepository
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

class CigarraServiceSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraService" when {

    "creating a new Cigarra" when {

      "CigarraRepository is able to create a new Cigarra" should {

        "return the new Cigarra guid" in {
          val expectedGuid = "some-guid"
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.save(any[Cigarra])).thenReturn(Some(expectedGuid))
          val service = new CigarraService(cigarraRepository)

          val cigarraName = "some-name"
          val guid = service.createCigarra(cigarraName).getOrElse("cigarra not created")

          guid mustEqual expectedGuid
        }
      }

      "CigarraRepository is not able to create a new Cigarra" should {

        "return a None" in {
          val expectedGuid = "some-guid"
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.save(any[Cigarra])).thenReturn(None)
          val service = new CigarraService(cigarraRepository)

          val cigarraName = "some-name"
          val guid = service.createCigarra(cigarraName)

          guid mustBe None
        }
      }
    }

    "retrieving an existing Cigarra by its guid" when {

      "CigarraRepository is not able to find a Cigarra" should {

        "return a None" in {
          val guid = java.util.UUID.randomUUID.toString
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findCigarra(any[String])).thenReturn(None)
          val service = new CigarraService(cigarraRepository)

          val cigarra = service.findCigarra(guid)

          cigarra mustBe None
        }
      }
    }
  }
}
