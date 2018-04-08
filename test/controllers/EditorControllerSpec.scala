package controllers

import org.scalatest.{MustMatchers, WordSpec}
import play.api.mvc.Results
import play.api.test.{FakeRequest, Helpers}
import scala.concurrent.duration._

import scala.concurrent.Await

class EditorControllerSpec extends WordSpec with MustMatchers {

  "EditorController" when {
    "a GET request for the Master editor page is made" should {
      "show return the Master editor html" in {
        val controller = new EditorController(Helpers.stubControllerComponents())
        val cigarraId = "some-id"

        val result = Await.result(controller.index(cigarraId)(FakeRequest()), 1.second)

        result mustEqual Results.Ok(views.html.editor())
      }
    }
  }

}
