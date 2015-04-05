package controllers

import controllers.common.ControllerHelper
import controllers.security.AuthenticationConfig
import db.DBExpense
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models._
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  def newExpense = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val expenseJR = ExpenseJson.jsonReaderSetUserId(loggedIn.userId)

    ControllerHelper.withJsonRequest { json =>
      val newList = json.as[Expense]
      val partProvider = (expId: Long) => {
        implicit val userExpenseJR = UserExpenseJoin.jsonReaderSetExpenseId(expId)
        (json \ ExpenseJson.JSON_PARTICIPANTS).as[Seq[UserExpenseJoin]]
      }

      ExpenseOperator.createExpense(newList, partProvider) match {
        case Left((list, parts)) =>
          implicit val insertedExpenseJW = ExpenseJson.insertedFullJsonWriter(parts)
          Ok(Json.toJson(list))
        case Right(err) =>
          BadRequest(err.toJson)
      }
    }
  }

  def editExpense(eid: Long) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val expenseJR = ExpenseJson.NewJsonReader
    implicit val insertedExpenseJW = ExpenseJson.InsertedJsonWriter

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
