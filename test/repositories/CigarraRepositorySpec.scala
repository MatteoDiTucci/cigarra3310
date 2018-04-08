package repositories

import domain.Cigarra
import org.scalatest.{MustMatchers, WordSpec}

class CigarraRepositorySpec extends WordSpec with MustMatchers {
  "CigarraRepository" when {

    "saving a new Cigarra" should {

      "persist the Cigarra and return its guid" in {
        val cigarra = Cigarra("some-name")
        val cigarraRepository = new CigarraRepository()

        val guid = cigarraRepository.save(cigarra)

        guid mustBe defined
      }
    }
  }
}
