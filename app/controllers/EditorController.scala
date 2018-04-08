package controllers

import javax.inject._

import play.api.mvc._

@Singleton
class EditorController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index(cigarraId: String) = Action {
    Ok(views.html.editor())
  }
}
