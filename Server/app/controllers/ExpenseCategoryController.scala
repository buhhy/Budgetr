package controllers

import controllers.common.Errors.NoJsonError
import controllers.security.AuthenticationConfig
import db.DBExpenseCategory
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{ExpenseCategory, NormalUser}
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseCategoryController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val eciw = ExpenseCategory.InsertedJsonWriter

  def newExpenseCategory = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val ecjr = ExpenseCategory.jsonReaderFromUserId(loggedIn.userId)

    request.body.asJson match {
      case Some(json) =>
        val newCat = (json \ "value").as[ExpenseCategory]
        DBExpenseCategory.insert(newCat) match {
          case Left(insertedCat) =>
            Ok(Json.toJson(insertedCat))
          case Right(error) =>
            BadRequest(error.toJson)
        }
      case None =>
        BadRequest(NoJsonError)
    }
  }
}
