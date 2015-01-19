package controllers

import controllers.security.AuthenticationConfig
import db.DBUser
import jp.t2v.lab.play2.auth.AuthElement
import models.User
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object UserController extends Controller with AuthElement with AuthenticationConfig {
  implicit val jw = User.JsonWriter
  implicit val jr = User.JsonReader

  def newUser = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        val newUser = (json \ "value").as[User]
        DBUser.insert(newUser) match {
          case Left(result) =>
            Ok(Json.toJson(result))
          case Right(error) =>
            BadRequest(error.toJson)
        }
      case None =>
        BadRequest("")
    }
  }

  def currentUser = StackAction { implicit request =>
    Ok(Json.toJson(loggedIn))
  }
}
