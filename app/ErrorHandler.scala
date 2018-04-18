import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import javax.inject.Singleton

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(
      Ok(views.html.error("Sorry, an error occurred"))
    )

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    Future.successful(
      Ok(views.html.error("Sorry, an error occurred"))
    )
}
