import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ErrorHandlerSpec extends WordSpec with MustMatchers {
  "ErrorHandler" when {

    "a server error occurs" should {

      "return a 200 with an error message" in {
        val errorHandler = new ErrorHandler()

        val result = errorHandler.onServerError(FakeRequest(), new RuntimeException())

        status(result) mustBe OK
        contentAsString(result) contains "Sorry, an error occurred"
      }
    }

    "a client error occurs" should {

      "return a 200 with an error message" in {
        val errorHandler = new ErrorHandler()

        val someStatusCode = 0
        val result = errorHandler.onClientError(FakeRequest(), someStatusCode, "some-message")

        status(result) mustBe OK
        contentAsString(result) contains "Sorry, an error occurred"
      }
    }
  }
}
