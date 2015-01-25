package controllers

import controllers.security.AuthenticationConfig
import db.{DBExpenseList, DBUserExpenseJoin}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{Expense, ExpenseList, NormalUser, UserExpenseJoin}
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseListController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val eljw = ExpenseList.NewJsonWriter
  implicit val ieljw = ExpenseList.InsertedJsonWriter
  implicit val ejw = Expense.NewJsonWriter

  def newExpenseList = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val userId = loggedIn.userId
    implicit val eljr = ExpenseList.jsonReaderFromUserId(userId)

    request.body.asJson match {
      case Some(json) =>
        val newList = (json \ "value").as[ExpenseList]
        DBExpenseList.insert(newList) match {
          case Left(insertedList) =>
            // Also add the creator to the expense list memberships list.
            DBUserExpenseJoin.insert(UserExpenseJoin(userId, insertedList.expListId)) match {
              case Left(insertedJoin) =>
                Ok(Json.toJson(insertedList))
              case Right(error) =>
                BadRequest(error.toJson)
            }
          case Right(error) =>
            BadRequest(error.toJson)
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

  def getExpenseLists = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val writer = ExpenseList.InsertedJsonWriter
    DBExpenseList.filterLists(loggedIn.userId) match {
      case Left(result) =>
        Ok(Json.toJson(result.map(writer.writes)))
      case Right(err) =>
        BadRequest(err.toJson)
    }
  }

  def addUserToExpenseList(eid: Long, uid: Long) =
    StackAction(AuthorityKey -> NormalUser) { implicit request =>
      DBUserExpenseJoin.insert(UserExpenseJoin(uid, eid)) match {
        case Left(insertedJoin) =>
          Ok(Json.obj())
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
}
