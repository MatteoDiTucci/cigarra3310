package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.{CigarraService, LevelService}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@Singleton
class LevelController @Inject()(cigarraService: CigarraService, levelService: LevelService)(cc: ControllerComponents)(
    implicit ex: ExecutionContext)
    extends AbstractController(cc) {

  private val LEVEL_SOLUTION_FROM_KEY = "solution"

  def solveLevel(cigarraId: String, levelId: String): Action[AnyContent] = Action.async {
    request: Request[AnyContent] =>
      getSolutionFromBody(request).fold(Future.successful(BadRequest("Solution not found")))(solution =>
        isSolutionCorrect(cigarraId, levelId, solution).flatMap { isCorrect =>
          if (isCorrect) {
            redirectToNextLevel(cigarraId, levelId)
          } else {
            redirectToCurrentLevel(cigarraId, levelId)
          }
      })
  }

  private def redirectToCurrentLevel(cigarraId: String, levelId: String) =
    Future.successful(SeeOther(s"/cigarra/$cigarraId/level/$levelId"))

  private def redirectToNextLevel(cigarraId: String, levelId: String) =
    levelService.findNextLevel(levelId).flatMap { maybeLevel =>
      maybeLevel.fold(Future.successful(Ok(views.html.end("The End"))))(level =>
        Future.successful(SeeOther(s"/cigarra/$cigarraId/level/${level.id}")))
    }

  private def isSolutionCorrect(cigarraId: String, levelId: String, solution: String) =
    levelService.solveLevel(cigarraId, levelId, solution)

  def level(cigarraId: String, levelId: String): Action[AnyContent] = Action.async {
    for {
      cigarra <- cigarraService.findCigarra(cigarraId)
      level <- levelService.findLevel(levelId)
      result <- Future.successful(Ok(views.html.level(cigarraId, cigarra.name, levelId, level.description)))
    } yield result

  }

  private def getSolutionFromBody(request: Request[AnyContent]) =
    for {
      params <- request.body.asFormUrlEncoded
      solution <- getSolution(params)
    } yield solution

  private def getSolution(parametersMap: Map[String, Seq[String]]): Option[String] =
    Try(parametersMap(LEVEL_SOLUTION_FROM_KEY)) match {
      case Failure(_)      => None
      case Success(values) => Some(values.head)
    }

}
