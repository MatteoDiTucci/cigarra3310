package controllers

import domain.Cigarra
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test._
import services.CigarraService

class EditorControllerSpec extends WordSpec with MustMatchers with MockitoSugar with Results {
  "EditorController" when {

    "a GET request for the Master editor page is made" should {

      "show return the Master editor html" in {
        val cigarraName = "some-name"
        val cigarraGuid = "some-cigarra-guid"
        val cigarraService = mock[CigarraService]
        when(cigarraService.findCigarra(any[String])).thenReturn(Some(Cigarra(name = cigarraName)))
        val controller = new EditorController(cigarraService)(Helpers.stubControllerComponents())

        val result = controller.index(cigarraGuid)(FakeRequest())

        contentAsString(result) contains cigarraName
      }
    }
  }

}
