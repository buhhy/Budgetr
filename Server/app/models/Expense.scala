package models

import controllers.common.Errors.ResultWithError
import db.{DBExpense, DBUserExpenseJoin}
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, Reads, JsObject, JsPath}

case class Expense(
    location: String, desc: String, parentListId: Long,
    creatorUserId: Long, categoryId: Long, amount: Int) {
  def toJson: JsObject = Expense.JsonWriter.writes(this)
}

case class InsertedExpense(expId: Long, createDate: DateTime, expense: Expense) {
  def toJson: JsObject = InsertedExpense.JsonWriter.writes(this)
}

case class InsertedExpenseWithAllData(
    expense: InsertedExpense, participants: Seq[InsertedUserExpenseJoin])

object Expense {
  val JSON_LOCATION = "location"
  val JSON_DESCRIPTION = "description"
  val JSON_PARENT_LIST_ID = "parentListId"
  val JSON_CREATOR_USER_ID = "creatorUserId"
  val JSON_CATEGORY_ID = "categoryId"
  val JSON_AMOUNT = "amount"
  val JSON_PARTICIPANTS = "participants"

  private val JsonReaderBase = ((JsPath \ JSON_LOCATION).read[String]
      and (JsPath \ JSON_DESCRIPTION).read[String]
      and (JsPath \ JSON_PARENT_LIST_ID).read[Long]
      and (JsPath \ JSON_CATEGORY_ID).read[Long]
      and (JsPath \ JSON_AMOUNT).read[Int])

  private val JsonWriterBase = ((JsPath \ JSON_LOCATION).write[String]
      and (JsPath \ JSON_DESCRIPTION).write[String]
      and (JsPath \ JSON_PARENT_LIST_ID).write[Long]
      and (JsPath \ JSON_CREATOR_USER_ID).write[Long]
      and (JsPath \ JSON_CATEGORY_ID).write[Long]
      and (JsPath \ JSON_AMOUNT).write[Int])

  /**
   * Default JSON reader for a new Expense object with no ID or create date.
   */
  val JsonReader = (JsonReaderBase ~ (JsPath \ JSON_CREATOR_USER_ID).read[Long])
      .apply { (loc, desc, pid, ecid, am, cid) => Expense(loc, desc, pid, cid, ecid, am) }

  val JsonWriter = JsonWriterBase.apply(unlift(Expense.unapply))

  def jsonReaderSetUserId(userId: Long) =
    JsonReaderBase.apply { (loc, desc, pid, ecid, am) => Expense(loc, desc, pid, userId, ecid, am) }

  def createExpense(
      newList: Expense,
      participantProvider: Long => Seq[UserExpenseJoin]):
  ResultWithError[InsertedExpenseWithAllData] = {

    DBExpense.insert(newList) match {
      case Left(insertedList) =>
        val parts = participantProvider(insertedList.expId)
        // If no participants are provided, then add the expense list creator as a default
        // participant that paid for the full amount and is responsible for the full amount.
        val filledParts = if (parts.isEmpty) {
          Seq(new UserExpenseJoin(newList.creatorUserId, insertedList.expId, 1.0, 1.0))
        } else {
          parts
        }

        DBUserExpenseJoin.insertAll(filledParts) match {
          case Left(insParts) =>
            Left(InsertedExpenseWithAllData(insertedList, insParts))
          case Right(error) =>
            Right(error)
        }
      case Right(error) =>
        Right(error)
    }
  }
}

object InsertedExpense {
  val JSON_EXPENSE_ID = "expenseId"
  val JSON_CREATE_DATE = "createDate"

  /**
   * JSON writer for an Expense object that has an ID and create date.
   */
  val JsonWriter = ((JsPath \ JSON_EXPENSE_ID).write[Long]
      and (JsPath \ JSON_CREATE_DATE).write[DateTime]
      and Expense.JsonWriter).apply(unlift(InsertedExpense.unapply))
}

object InsertedExpenseWithAllData {
  implicit val insertedUserExpenseJoinJW = InsertedUserExpenseJoin.JsonWriter
  val JsonWriter = (InsertedExpense.JsonWriter ~
      (JsPath \ Expense.JSON_PARTICIPANTS).write[Seq[InsertedUserExpenseJoin]])
      .apply(unlift(InsertedExpenseWithAllData.unapply))


//  def insertedFullJsonWriter(participants: Seq[InsertedUserExpenseJoin]) = {
//    implicit val insertedUserExpenseJW = UserExpenseJoin.InsertedJsonWriter
//    (JsonWriter ~ (JsPath \ JSON_PARTICIPANTS)
//        .write[Seq[InsertedUserExpenseJoin]])
//        .apply { exp: InsertedExpense => (exp, participants) }
//  }
}
