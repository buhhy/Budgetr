package controllers

import controllers.security.AuthenticationConfig
import db.{DBUserExpenseJoin, DBExpense}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{UserExpenseJoin, Expense, NormalUser}
import play.api.libs.json.Json
import play.api.mvc.Controller
import controllers.common.ControllerHelper

object ExpenseController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  def newExpense = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val expenseJR = Expense.jsonReaderSetUserId(loggedIn.userId)

    ControllerHelper.withJsonRequest { json =>
      val newList = json.as[Expense]

      DBExpense.insert(newList) match {
        case Left(result) =>
          implicit val userExpenseJR = UserExpenseJoin.jsonReaderSetExpenseId(result.expId)
          val participants = (json \ Expense.JSON_PARTICIPANTS).as[Seq[UserExpenseJoin]]

          DBUserExpenseJoin.insertAll(participants) match {
            case Left(parts) =>
              implicit val insertedExpenseJW = Expense.insertedFullJsonWriter(parts)
              Ok(Json.toJson(result))
            case Right(error) =>
              BadRequest(error.toJson)
          }
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
  }

  def editExpense(eid: Long) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val expenseJR = Expense.NewJsonReader
    implicit val insertedExpenseJW = Expense.InsertedJsonWriter

    ControllerHelper.withJsonRequest { json =>
      val updatedList = json.as[Expense]
      DBExpense.update(eid, updatedList) match {
        case Left(result) =>
          Ok(Json.toJson(result))
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
  }
}
