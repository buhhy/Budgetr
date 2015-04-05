package controllers

import controllers.security.AuthenticationConfig
import db.{DBExpenseList, DBUserExpenseListJoin}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models._
import play.api.libs.json.Json
import play.api.mvc.Controller
import controllers.common.{Errors, ControllerHelper}

object ExpenseListController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val expenseListJW = ExpenseList.JsonWriter
  implicit val insertedExpenseListJW = InsertedExpenseList.JsonWriter
  implicit val expenseJW = ExpenseJson.NewJsonWriter
  implicit val insertedExpenseCategoryJW = InsertedExpenseCategory.JsonWriter

  def newExpenseList = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val userId = loggedIn.userId
    implicit val eljr = ExpenseList.jsonReaderFromUserId(userId)

    ControllerHelper.withJsonRequest { json =>
      val newList = json.as[ExpenseList]
      DBExpenseList.insert(newList) match {
        case Left(insertedList) =>
          // Also add the creator to the expense list memberships list.
          DBUserExpenseListJoin.insert(UserExpenseListJoin(userId, insertedList.expListId)) match {
            case Left(insertedJoin) =>
              Ok(Json.toJson(insertedList))
            case Right(error) =>
              BadRequest(error.toJson)
          }
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
  }

  def getExpenseListById(id: Long) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    DBExpenseList.find(id) match {
      case Left(result) =>
        // Get the expenses associated with the selected expense list ID.
        val expenseResults = DBExpenseList.findAssociatedExpenses(id)
        // Get the expense categories associated with the selected expense list ID.
        val categoryResults = DBExpenseList.findAssociatedExpenseCategories(id)
        // Get the user members associated with the selected expense list ID.
        val memberResults = DBExpenseList.findAssociatedUsers(id)

        Errors.compose(Errors.compose(expenseResults, categoryResults), memberResults) match {
          case Left(((expenses, categories), members)) =>
            Ok(result.toJson(expenses, categories, members))
          case Right(err) =>
            BadRequest(err.toJson)
        }
      case Right(err) =>
        BadRequest(err.toJson)
    }
  }

  def getExpenseLists = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    DBExpenseList.filterLists(loggedIn.userId) match {
      case Left(result) =>
        Ok(Json.toJson(result))
      case Right(err) =>
        BadRequest(err.toJson)
    }
  }

  def addUserToExpenseList(eid: Long, uid: Long) =
    StackAction(AuthorityKey -> NormalUser) { implicit request =>
      DBUserExpenseListJoin.insert(UserExpenseListJoin(uid, eid)) match {
        case Left(insertedJoin) =>
          Ok(Json.obj())
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
}
