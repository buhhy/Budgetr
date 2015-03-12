package controllers

import controllers.common.Errors.NoJsonError
import controllers.security.AuthenticationConfig
import db.{DBExpenseList, DBUserExpenseListJoin}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models._
import play.api.libs.json.Json
import play.api.mvc.Controller

object ExpenseListController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val eljw = ExpenseList.NewJsonWriter
  implicit val ieljw = ExpenseList.InsertedJsonWriter
  implicit val ejw = Expense.NewJsonWriter
  implicit val ecjw = ExpenseCategory.InsertedJsonWriter

  def newExpenseList = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val userId = loggedIn.userId
    implicit val eljr = ExpenseList.jsonReaderFromUserId(userId)

    request.body.asJson match {
      case Some(json) =>
        val newList = (json \ "value").as[ExpenseList]
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
      case None =>
        BadRequest(NoJsonError)
    }
  }

  def getExpenseListById(id: Long) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    DBExpenseList.find(id) match {
      case Left(result) =>
        // Get the expenses associated with the selected expense list ID.
        val expenseResults = DBExpenseList.findAssociatedExpenses(id)
        // Get the expense categories associated with the selected expense list ID.
        val categoryResults = DBExpenseList.findAssociatedExpenseCategories(id)

        (expenseResults, categoryResults) match {
          case (Left(expenses), Left(categories)) =>
            Ok(result.toJson(expenses, categories))
          case (Right(err1), Right(err2)) =>
            BadRequest(Json.toJson(Seq(err1.toJson, err2.toJson)))
          case (_, Right(err)) =>
            BadRequest(err.toJson)
          case (Right(err), _) =>
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
      DBUserExpenseListJoin.insert(UserExpenseListJoin(uid, eid)) match {
        case Left(insertedJoin) =>
          Ok(Json.obj())
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
}
