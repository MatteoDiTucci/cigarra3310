package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.{CigarraService, LevelService}

import scala.util.{Failure, Success, Try}

@Singleton
class CigarraController @Inject()(cigarraService: CigarraService, levelService: LevelService)(cc: ControllerComponents)
    extends AbstractController(cc) {

  private val CIGARRA_NAME_FROM_KEY = "name"

  def create(): Action[AnyContent] = Action { request: Request[AnyContent] =>
    getCigarraName(request).fold(BadRequest("An error as occurred"))(createCigarraWithName)
  }

  private def createCigarraWithName(name: String) = {
    val guid = cigarraService.createCigarra(name)
    redirectToEditorWithGuid(guid)
  }

  private def redirectToEditorWithGuid(guid: String) =
    SeeOther(s"/cigarra/$guid/level")

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

  def findFirstLevel(cigarraGuid: String) = Action {
    val firstLevel = levelService.findFirstLevel(cigarraGuid)
    firstLevel.fold(BadRequest("Cigarra not found"))(level => SeeOther(s"/cigarra/$cigarraGuid/level/${level.guid}"))
  }

}
