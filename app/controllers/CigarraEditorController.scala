package controllers

import javax.inject._
import play.api.mvc._
import services.{CigarraService, LevelService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class CigarraEditorController @Inject()(cigarraService: CigarraService, levelService: LevelService)(
    cc: ControllerComponents)(implicit ex: ExecutionContext)
    extends AbstractController(cc) {

  private val LEVEL_DESCRIPTION_FROM_KEY = "description"
  private val LEVEL_SOLUTION_FROM_KEY = "solution"

  def levelEditor(cigarraId: String): Action[AnyContent] = Action.async {
    cigarraService
      .findCigarra(cigarraId)
      .map(someCigarra => Ok(views.html.editor(someCigarra.name, cigarraId)))
  }
  def createLevel(cigarraId: String): Action[AnyContent] = Action.async { request =>
    getLevelFromRequest(request).fold(Future.successful(BadRequest("Missing level description or solution")))(level =>
      for {
        levelId <- levelService.createLevel(cigarraId, level.description, level.solution)
        _ <- cigarraService.setFirstLevel(cigarraId, levelId)
        cigarra <- cigarraService.findCigarra(cigarraId)
        result = Ok(views.html.editor(cigarra.name, cigarraId))
      } yield result)
  }

  private def getLevelFromRequest(request: Request[AnyContent]): Option[Level] =
    for {
      requestParams <- request.body.asFormUrlEncoded
      description <- getDescriptionFromBody(requestParams)
      solution <- getSolutionFromBody(requestParams)
    } yield { Level(description, solution) }

  private def getDescriptionFromBody(parametersMap: Map[String, Seq[String]]) =
    Try(parametersMap(LEVEL_DESCRIPTION_FROM_KEY)) match {
      case Failure(_)      => None
      case Success(values) => Some(values.head)
    }

  private def getSolutionFromBody(parametersMap: Map[String, Seq[String]]) =
    Try(parametersMap(LEVEL_SOLUTION_FROM_KEY)) match {
      case Failure(_)      => None
      case Success(values) => Some(values.head)
    }

  case class Level(solution: String, description: String)
}
