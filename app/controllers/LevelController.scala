package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.{CigarraService, LevelService}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@Singleton
class LevelController @Inject()(cigarraService: CigarraService, levelService: LevelService)(cc: ControllerComponents)
    extends AbstractController(cc) {

  private val LEVEL_SOLUTION_FROM_KEY = "solution"

  def solveLevel(cigarraGuid: String, levelGuid: String): Action[AnyContent] = Action { request: Request[AnyContent] =>
    isSolutionCorrect(cigarraGuid, levelGuid, request) match {
      case None        => BadRequest("Level not found")
      case Some(false) => redirectToCurrentLevel(cigarraGuid, levelGuid)
      case Some(true)  => redirectToNextLevel(cigarraGuid, levelGuid)
    }
  }

  private def redirectToCurrentLevel(cigarraGuid: String, levelGuid: String) =
    SeeOther(s"/cigarra/$cigarraGuid/level/$levelGuid")

  private def redirectToNextLevel(cigarraGuid: String, levelGuid: String) =
    levelService.findNextLevel(cigarraGuid, levelGuid) match {
      case None        => Ok(views.html.end("The End"))
      case Some(level) => SeeOther(s"/cigarra/$cigarraGuid/level/${level.guid.getOrElse("level-guid-not-defined")}")
    }

  private def isSolutionCorrect(cigarraGuid: String, levelGuid: String, request: Request[AnyContent]) =
    for {
      solution <- getSolutionFromBody(request)
      isSolved <- levelService.solveLevel(cigarraGuid, levelGuid, solution)
    } yield isSolved

  def level(cigarraGuid: String, levelGuid: String) = Action {
    (for {
      cigarra <- Await.result(cigarraService.findCigarra(cigarraGuid), 1.second)
      level <- levelService.findLevel(cigarraGuid, levelGuid)
    } yield (cigarra, level)) match {
      case None => BadRequest("Cigarra or level not found")
      case Some((cigarra, level)) =>
        Ok(views.html.level(cigarraGuid, cigarra.name, levelGuid, level.description))
    }
  }

  private def getSolutionFromBody(request: Request[AnyContent]): Option[String] =
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
