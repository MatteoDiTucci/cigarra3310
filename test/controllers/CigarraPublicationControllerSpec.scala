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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CigarraPublicationControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraPublicationController" when {

    "receiving a GET request to publish a Cigarra" should {

      "return the url for playing the Cigarra" in {
        val cigarraService = mock[CigarraService]
        val cigarra = Cigarra("some-cigarra-id", "some-cigarra-name")
        when(cigarraService.findCigarra(any[String]))
          .thenReturn(Future.successful(cigarra))
        val controller = createController(cigarraService)

        val result = controller.index("some-cigarra-id")(FakeRequest())

        contentAsString(result) must include("some-cigarra-id")
        contentAsString(result) must include("some-cigarra-name")
      }
    }
  }

  private def createController(cigarraService: CigarraService) =
    new CigarraPublicationController(cigarraService, mock[Configuration])(stubControllerComponents())
}
