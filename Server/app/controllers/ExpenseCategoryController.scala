package controllers

import controllers.security.AuthenticationConfig
import db.DBExpenseCategory
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{ExpenseCategory, NormalUser}
import play.api.libs.json.Json
import play.api.mvc.Controller
import controllers.common.ControllerHelper

object ExpenseCategoryController extends Controller with LoginLogout
    with AuthElement with AuthenticationConfig {

  implicit val eciw = ExpenseCategory.InsertedJsonWriter

  def getOrNewExpenseCategory = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    implicit val expenseCategoryJR = ExpenseCategory.jsonReaderFromUserId(loggedIn.userId)

    ControllerHelper.withJsonRequest { json =>
      val newCat = json.as[ExpenseCategory]

      // Try to find the category first
      DBExpenseCategory.find(newCat.name, newCat.parentListId) match {
        case Left(Some(exp)) =>
          Ok(Json.toJson(exp))
        case Left(None) =>
          // If it isn't found, create it
          DBExpenseCategory.insert(newCat) match {
            case Left(insertedCat) =>
              Ok(Json.toJson(insertedCat))
            case Right(error) =>
              BadRequest(error.toJson)
          }
        case Right(error) =>
          BadRequest(error.toJson)
      }
    }
  }
}
