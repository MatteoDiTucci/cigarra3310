package controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import services.CigarraService

@Singleton
class CigarraPublicationController @Inject()(cigarraService: CigarraService, playConfiguration: Configuration)(
    cc: ControllerComponents)
    extends AbstractController(cc) {

  def index(cigarraGuid: String) = Action {
    cigarraService
      .findCigarra(cigarraGuid)
      .fold(InternalServerError("Cigarra not found"))(cigarra =>
        Ok(views.html.publication(cigarra.name, s"/cigarra/$cigarraGuid")))
  }

}
