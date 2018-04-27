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

  def solveLevel(cigarraGuid: String, levelGuid: String): Action[AnyContent] = Action.async {
    request: Request[AnyContent] =>
      getSolutionFromBody(request).fold(Future.successful(BadRequest("Solution not found")))(solution =>
        isSolutionCorrect(cigarraGuid, levelGuid, solution).flatMap { isCorrect =>
          if (isCorrect) {
            redirectToNextLevel(cigarraGuid, levelGuid)
          } else {
            redirectToCurrentLevel(cigarraGuid, levelGuid)
          }
      })
  }

  private def redirectToCurrentLevel(cigarraGuid: String, levelGuid: String) =
    Future.successful(SeeOther(s"/cigarra/$cigarraGuid/level/$levelGuid"))

  private def redirectToNextLevel(cigarraGuid: String, levelGuid: String) =
    levelService.findNextLevel(levelGuid).flatMap { maybeLevel =>
      maybeLevel.fold(Future.successful(Ok(views.html.end("The End"))))(level =>
        Future.successful(SeeOther(s"/cigarra/$cigarraGuid/level/${level.guid}")))
    }

  private def isSolutionCorrect(cigarraGuid: String, levelGuid: String, solution: String) =
    levelService.solveLevel(cigarraGuid, levelGuid, solution)

  def level(cigarraGuid: String, levelGuid: String) = Action {
    val maybeCigarra = Await.result(cigarraService.findCigarra(cigarraGuid), 1.second)
    val maybeLevel = Await.result(levelService.findLevel(levelGuid), 1.second)

    (for {
      cigarraName <- maybeCigarra.map(_.name)
      levelDescription <- maybeLevel.map(_.description)
    } yield (cigarraName, levelDescription)) match {
      case None => BadRequest("Cigarra or level not found")
      case Some((cigarraName, levelDescription)) =>
        Ok(views.html.level(cigarraGuid, cigarraName, levelGuid, levelDescription))
    }
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
