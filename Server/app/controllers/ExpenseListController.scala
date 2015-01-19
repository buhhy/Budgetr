package controllers

import controllers.security.AuthenticationConfig
import db.{DBExpense, DBExpenseList}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{Expense, ExpenseList, NormalUser}
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseListController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val eljw = ExpenseList.JsonWriter
  implicit val ejw = Expense.JsonWriter

  def newExpenseList = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    // TODO(tlei): not cool
    implicit val eljr = ExpenseList.buildJsonReader(loggedIn.userId.get)

    request.body.asJson match {
      case Some(json) =>
        val newList = (json \ "value").as[ExpenseList]
        DBExpenseList.save(newList) match {
          case Right(error) =>
            BadRequest(error.toJson)
          case Left(result) =>
            Ok(Json.toJson(result))
        }
      case None =>
        BadRequest("")
    }
  }

  def getExpenseListById(id: Long) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    DBExpenseList.find(id) match {
      case Left(result) =>
        DBExpenseList.findAssociatedExpenses(id) match {
          case Left(expenses) =>
            Ok(result.toJson(expenses))
          case Right(err) =>
            BadRequest(err.toJson)
        }
      case Right(err) =>
        BadRequest(err.toJson)
    }
  }
}
