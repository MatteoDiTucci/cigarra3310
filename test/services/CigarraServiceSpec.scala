package services

import org.scalatest.{MustMatchers, WordSpec}

class CigarraServiceSpec extends WordSpec with MustMatchers {
  "CigarraService" when {

    "provided with a name to create a new Cigarra" should {

      "return the new Cigarra guid" in {
        val expectedGuid = "some-guid"
        val service = new CigarraService()

        val cigarraName = "some-name"
        val guid = service.createCigarra(cigarraName).getOrElse("cigarra not created")

        guid mustEqual expectedGuid
      }
    }
  }
}
