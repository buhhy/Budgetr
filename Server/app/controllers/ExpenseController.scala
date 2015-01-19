package controllers

import controllers.security.AuthenticationConfig
import db.DBExpense
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{Expense, NormalUser}
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val jw = Expense.JsonWriter

  def newExpense = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    // TODO(tlei): not cool
    implicit val jr = Expense.buildJsonReader(loggedIn.userId.get)

    request.body.asJson match {
      case Some(json) =>
        val newList = (json \ "value").as[Expense]
        DBExpense.save(newList) match {
          case Left(result) =>
            Ok(Json.toJson(result))
          case Right(error) =>
            BadRequest(error.toJson)
        }
      case None =>
        BadRequest("")
    }
  }
}
