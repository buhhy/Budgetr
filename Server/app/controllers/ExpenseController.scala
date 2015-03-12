package controllers

import controllers.security.AuthenticationConfig
import db.{DBUserExpenseJoin, DBUserExpenseListJoin, DBExpense}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{UserExpenseJoin, UserExpenseListJoin, Expense, NormalUser}
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val ejw = Expense.NewJsonWriter
  implicit val iejw = Expense.InsertedJsonWriter

  def newExpense = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val ejr = Expense.jsonReaderFromUserId(loggedIn.userId)
    implicit val uejjr = UserExpenseJoin.JsonReader

    request.body.asJson match {
      case Some(json) =>
        val value = json \ "value"
        val newList = value.as[Expense]
        val participants = (value \ "participants").as[Seq[UserExpenseJoin]]

        DBExpense.insert(newList) match {
          case Left(result) =>
            DBUserExpenseJoin.insert()
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
