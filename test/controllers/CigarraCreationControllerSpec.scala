package controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.api.http.Status.SEE_OTHER
import services.CigarraService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

class CigarraCreationControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraCreationController" when {

    "" should {
      val request = FakeRequest("POST", "/")
        .withFormUrlEncodedBody("name" -> "some-name")

      "redirect to the Master editor page" in {
        val cigarraService = mock[CigarraService]
        when(cigarraService.createCigarra(any[String])).thenReturn(Some("some-id"))

        val controller = new CigarraCreationController(cigarraService)(Helpers.stubControllerComponents())

        val result = controller.create()(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get must endWith("/cigarra/some-id/editor")
      }
    }
  }
}
