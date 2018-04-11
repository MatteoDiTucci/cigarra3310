package controllers

import java.util.UUID

import javax.inject._
import play.api.mvc._
import services.{CigarraService, LevelService}

import scala.util.{Failure, Success, Try}

@Singleton
class CigarraEditorController @Inject()(cigarraService: CigarraService, levelService: LevelService)(
    cc: ControllerComponents)
    extends AbstractController(cc) {

  private val LEVEL_DESCRIPTION_FROM_KEY = "description"
  private val LEVEL_SOLUTION_FROM_KEY = "solution"

  def index(cigarraGuid: String) = Action {
    cigarraService.findCigarra(cigarraGuid) match {
      case Some(cigarra) => Ok(views.html.editor(cigarra.name, cigarraGuid))
      case None          => InternalServerError("An Error has occurred")
    }
  }

  def createLevel(cigarraGuid: String): Action[AnyContent] = Action { request =>
    cigarraService
      .findCigarra(cigarraGuid)
      .fold(BadRequest("Cigarra not found"))(
        cigarra =>
          getDescriptionAndSolutionFromRequest(request).fold(BadRequest("Missing description or solution"))(
            descriptionAndSolution =>
              createLevelWithDescriptionAndSolution(cigarra.guid.getOrElse("no-cigarra-guid"), descriptionAndSolution)
                .fold(InternalServerError("An error occurred"))(_ => Ok(views.html.editor(cigarra.name, cigarraGuid)))))
  }

  private def createLevelWithDescriptionAndSolution(cigarraGuid: String, descriptionAndSolution: (String, String)) =
    levelService
      .createLevel(cigarraGuid, descriptionAndSolution._1, descriptionAndSolution._2)

  private def getDescriptionAndSolutionFromRequest(request: Request[AnyContent]) =
    for {
      requestParams <- request.body.asFormUrlEncoded
      levelDescription <- getDescriptionFromBody(requestParams)
      levelSolution <- getSolutionFromBody(requestParams)
    } yield { (levelDescription, levelSolution) }

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
}
