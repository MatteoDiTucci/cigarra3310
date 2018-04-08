package controllers

import javax.inject._
import play.api.mvc._
import services.CigarraService

@Singleton
class EditorController @Inject()(cigarraService: CigarraService)(cc: ControllerComponents)
    extends AbstractController(cc) {

  def index(cigarraGuid: String) = Action {
    cigarraService.findCigarra(cigarraGuid) match {
      case Some(cigarra) => Ok(views.html.editor(cigarra.name))
    }
  }
}
