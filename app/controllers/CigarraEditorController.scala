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

  def index(cigarraGuid: String): Action[AnyContent] = Action.async {
    cigarraService
      .findCigarra(cigarraGuid)
      .map(someCigarra => Ok(views.html.editor(someCigarra.get.name, cigarraGuid)))
      .recoverWith {
        case _: Throwable => Future.successful(InternalServerError)
      }
  }
  def createLevel(cigarraGuid: String): Action[AnyContent] = Action.async { request =>
    cigarraService
      .findCigarra(cigarraGuid)
      .map { cigarra =>
        getDescriptionAndSolutionFromRequest(request).fold(BadRequest("Missing description or solution"))(
          descriptionAndSolution => {
            createLevelWithDescriptionAndSolution(cigarra.get.guid, descriptionAndSolution).map { levelGuid =>
              cigarraService.setFirstLevel(cigarra.get.guid, levelGuid)
            }
            Ok(views.html.editor(cigarra.get.name, cigarraGuid))
          })
      }
      .recoverWith {
        case _: Throwable => Future.successful(BadRequest("Missing description or solution"))
      }
  }

  private def createLevelWithDescriptionAndSolution(cigarraGuid: String,
                                                    descriptionAndSolution: (String, String)): Future[String] =
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
