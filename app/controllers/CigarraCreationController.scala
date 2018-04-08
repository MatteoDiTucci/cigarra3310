package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.CigarraService

@Singleton
class CigarraCreationController @Inject()(cigarraService: CigarraService)(cc: ControllerComponents)
    extends AbstractController(cc) {

  def create(): Action[AnyContent] = Action {
    val cigarraId = cigarraService.createCigarra()
    SeeOther(s"/cigarra/$cigarraId/editor")
  }
}
