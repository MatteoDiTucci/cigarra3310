package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.{CigarraService, LevelService}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Singleton
class CigarraController @Inject()(cigarraService: CigarraService, levelService: LevelService)(cc: ControllerComponents)(
    implicit ex: ExecutionContext)
    extends AbstractController(cc) {

  private val CIGARRA_NAME_FROM_KEY = "name"

  def create(): Action[AnyContent] = Action { request: Request[AnyContent] =>
    getCigarraName(request).fold(BadRequest("An error as occurred"))(createCigarraWithName)
  }

  private def createCigarraWithName(name: String) = {
    val id = cigarraService.createCigarra(name)
    redirectToEditorWithId(id)
  }

  private def redirectToEditorWithId(id: String) =
    SeeOther(s"/cigarra/$id/level")

  private def getCigarraName(request: Request[AnyContent]) =
    for {
      requestParams <- request.body.asFormUrlEncoded
      cigarraName <- getNameFromBody(requestParams)
    } yield cigarraName

  private def getNameFromBody(parametersMap: Map[String, Seq[String]]) =
    Try(parametersMap(CIGARRA_NAME_FROM_KEY)) match {
      case Failure(_)      => None
      case Success(values) => Some(values.head)
    }

  def findFirstLevel(cigarraId: String): Action[AnyContent] = Action.async {
    cigarraService.findFirstLevel(cigarraId).map { maybeFirstLevel =>
      maybeFirstLevel.fold(InternalServerError("Cigarra has level to play"))(level =>
        SeeOther(s"/cigarra/$cigarraId/level/${level.id}"))
    }
  }
}
