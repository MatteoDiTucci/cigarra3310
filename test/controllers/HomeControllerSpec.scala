package controllers

import javax.inject.Singleton
import org.scalatest.{MustMatchers, WordSpec}
import play.api.mvc.Results
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Await
import scala.concurrent.duration._

@Singleton
class HomeControllerSpec extends WordSpec with MustMatchers{

  "HomeController" when {
    "a GET request for the home page is made" should {
      "show the Cigarra3310 logo" in {
        val controller = new HomeController(Helpers.stubControllerComponents())

        val result = Await.result(controller.index()(FakeRequest()), 1.second)

        result mustEqual Results.Ok(views.html.home())
      }
    }
  }

}
