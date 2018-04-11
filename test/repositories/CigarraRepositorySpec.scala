package repositories

import java.util.UUID

import domain.Cigarra
import org.scalatest.{MustMatchers, WordSpec}

class CigarraRepositorySpec extends WordSpec with MustMatchers {
  "CigarraRepository" when {

    "saving a new Cigarra" should {

      "persist the Cigarra and return its guid" in {
        val cigarraRepository = new CigarraRepository()

        val guid = cigarraRepository.save(Cigarra(name = "some-name"))

        guid mustBe defined
        cigarraRepository.cigarras.get(UUID.fromString(guid.get)) mustBe defined
      }
    }

    "retrieving an existing Cigarra by its guid" when {

      "the Cigarra can be found" should {

        "return the related Cigarra" in {
          val guid = java.util.UUID.randomUUID

          val cigarraRepository = new CigarraRepository()
          cigarraRepository.cigarras.put(guid, Cigarra(Some(guid.toString), "some-name"))

          val cigarra = cigarraRepository.findCigarra(guid.toString)

          cigarra mustBe defined
          cigarra.get.guid.get mustEqual guid.toString
        }
      }

      "the Cigarra cannot be found" should {

        "return None" in {
          val guid = java.util.UUID.randomUUID
          val cigarraRepository = new CigarraRepository()

          val cigarra = cigarraRepository.findCigarra(guid.toString)

          cigarra mustBe None
        }
      }
    }
  }
}