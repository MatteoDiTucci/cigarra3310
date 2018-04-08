package controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.api.http.Status.SEE_OTHER
import services.CigarraService
import org.mockito.Mockito._

class CigarraCreationControllerSpec extends WordSpec with MustMatchers with MockitoSugar {
  "CigarraCreationController" when {

    "a GET request is performed to create a new Cigarra" should {

      "redirect to the Master editor page" in {

        val cigarraService = mock[CigarraService]
        when(cigarraService.createCigarra()).thenReturn("some-id")

        val controller = new CigarraCreationController(cigarraService)(Helpers.stubControllerComponents())

        val result = controller.create()(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get must endWith("/cigarra/some-id/editor")
      }
    }
  }
}
