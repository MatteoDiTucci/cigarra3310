package services

import org.scalatest.{MustMatchers, WordSpec}

class CigarraServiceSpec extends WordSpec with MustMatchers {
  "CigarraService" when {

    "creating a new Cigarra" should {

      "return the new Cigarra guid" in {
        val expectedGuid = "some-guid"
        val service = new CigarraService()

        val guid = service.createCigarra()

        guid mustEqual expectedGuid
      }
    }
  }
}
