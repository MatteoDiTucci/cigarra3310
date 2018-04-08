package services

import domain.Cigarra
import org.scalatest.{MustMatchers, WordSpec}
import repositories.CigarraRepository
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

class CigarraServiceSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraService" when {

    "provided with a name to create a new Cigarra" should {

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
  }
}
