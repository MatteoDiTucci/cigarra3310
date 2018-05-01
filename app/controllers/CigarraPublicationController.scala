package controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.CigarraService

import scala.concurrent.ExecutionContext

@Singleton
class CigarraPublicationController @Inject()(cigarraService: CigarraService, playConfiguration: Configuration)(
    cc: ControllerComponents)(implicit ex: ExecutionContext)
    extends AbstractController(cc) {

  def index(cigarraGuid: String): Action[AnyContent] = Action.async {
    cigarraService
      .findCigarra(cigarraGuid)
      .map(cigarra => Ok(views.html.publication(cigarra.name, s"/cigarra/$cigarraGuid")))
  }

}
