package controllers

import controllers.security.AuthenticationConfig
import db.{DBUserExpenseJoin, DBExpense}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{UserExpenseJoin, Expense, NormalUser}
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val ejw = Expense.NewJsonWriter
  implicit val iejw = Expense.InsertedJsonWriter

  def newExpense = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val jr = Expense.jsonReaderFromUserId(loggedIn.userId)

    request.body.asJson match {
      case Some(json) =>
        val newList = (json \ "value").as[Expense]
        DBExpense.insert(newList) match {
          case Left(result) =>
            Ok(Json.toJson(result))
          case Right(error) =>
            BadRequest(error.toJson)
        }
      case None =>
        BadRequest("")
    }
  }

  def editExpense(eid: Long) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val jr = Expense.NewJsonReader

    request.body.asJson match {
      case Some(json) =>
        val updatedList = (json \ "value").as[Expense]
        DBExpense.update(eid, updatedList) match {
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
