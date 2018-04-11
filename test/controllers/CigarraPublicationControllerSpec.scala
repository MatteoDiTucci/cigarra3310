package controllers

import domain.Cigarra
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CigarraService
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.Configuration

class CigarraPublicationControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraPublicationController" when {

    "receiving a GET request to publish a Cigarra" should {

      "return the url for playing to the Cigarra" in {
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(any[String])).thenReturn(Some(Cigarra(Some("some-guid"), "come-name")))
        val controller = createController(cigarraService)
        val cigarraGuid = "some-cigarra-guid"

        val result = controller.index(cigarraGuid)(FakeRequest())

        contentAsString(result) contains cigarraGuid
      }
    }
  }

  private def createController(cigarraService: CigarraService, configuration: Configuration = mock[Configuration]) =
    new CigarraPublicationController(cigarraService, configuration)(stubControllerComponents())
}
