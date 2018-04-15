package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CigarraService, LevelService}

@Singleton
class LevelController @Inject()(cigarraService: CigarraService, levelService: LevelService)(cc: ControllerComponents)
    extends AbstractController(cc) {
  def level(cigarraGuid: String, levelGuid: String) = Action {
    (for {
      cigarra <- cigarraService.findCigarra(cigarraGuid)
      level <- levelService.findFirstLevel(cigarraGuid)
    } yield (cigarra, level)) match {
      case None => BadRequest("Cigarra or level not found")
      case Some((cigarra, level)) =>
        Ok(views.html.level(cigarraGuid, cigarra.name, levelGuid, level.description))
    }
  }

}
