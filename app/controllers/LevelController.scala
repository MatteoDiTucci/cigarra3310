package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.{CigarraService, LevelService}

import scala.util.{Failure, Success, Try}

@Singleton
class LevelController @Inject()(cigarraService: CigarraService, levelService: LevelService)(cc: ControllerComponents)
    extends AbstractController(cc) {

  private val LEVEL_SOLUTION_FROM_KEY = "solution"

  def solveLevel(cigarraGuid: String, levelGuid: String): Action[AnyContent] = Action { request: Request[AnyContent] =>
    (for {
      solution <- getSolutionFromBody(request)
      nextLevel <- levelService.solveLevel(cigarraGuid, levelGuid, solution)
      nextLevelGuid <- nextLevel.guid
    } yield nextLevelGuid) match {
      case None => SeeOther(s"/cigarra/$cigarraGuid/level/$levelGuid")
      case Some(nextLevelGuid) =>
        SeeOther(s"/cigarra/$cigarraGuid/level/$nextLevelGuid")
    }
  }

  def level(cigarraGuid: String, levelGuid: String) = Action {
    (for {
      cigarra <- cigarraService.findCigarra(cigarraGuid)
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
