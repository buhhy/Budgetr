package controllers

import controllers.security.AuthenticationConfig
import db.DBUser
import jp.t2v.lab.play2.auth.AuthElement
import models.{InsertedUser, User}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import controllers.common.ControllerHelper

object UserController extends Controller with AuthElement with AuthenticationConfig {
  implicit val insertedUserJW = InsertedUser.JsonWriter
  implicit val userJR = User.JsonReader

  def newUser = Action { implicit request =>
    ControllerHelper.withJsonRequest { json =>
      val newUser = json.as[models.User]
      DBUser.insert(newUser) match {
        case Left(result) =>
          Ok(Json.toJson(result))
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
  }

  def currentUser = StackAction { implicit request =>
    Ok(Json.toJson(loggedIn))
  }
}
