package models

import controllers.common.ErrorType
import db.{DBExpense, DBUserExpenseJoin}
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsObject, JsPath}

case class Expense(
    location: String, desc: String, parentListId: Long,
    creatorUserId: Long, categoryId: Long, amount: Int) {
  def toJson: JsObject = ExpenseJson.NewJsonWriter.writes(this)
}

case class InsertedExpense(expId: Long, createDate: DateTime, expense: Expense) {
  def toJson: JsObject = ExpenseJson.InsertedJsonWriter.writes(this)
}

object ExpenseJson {
  val JSON_EXPENSE_ID = "expenseId"
  val JSON_CREATE_DATE = "createDate"
  val JSON_LOCATION = "location"
  val JSON_DESCRIPTION = "description"
  val JSON_PARENT_LIST_ID = "parentListId"
  val JSON_CREATOR_USER_ID = "creatorUserId"
  val JSON_CATEGORY_ID = "categoryId"
  val JSON_AMOUNT = "amount"
  val JSON_PARTICIPANTS = "participants"


  private val JsonReaderBase = (JsPath \ JSON_LOCATION).read[String] and
      (JsPath \ JSON_DESCRIPTION).read[String] and
      (JsPath \ JSON_PARENT_LIST_ID).read[Long] and
      (JsPath \ JSON_CATEGORY_ID).read[Long] and
      (JsPath \ JSON_AMOUNT).read[Int]

  private val JsonWriterBase = (JsPath \ JSON_LOCATION).write[String] and
      (JsPath \ JSON_DESCRIPTION).write[String] and
      (JsPath \ JSON_PARENT_LIST_ID).write[Long] and
      (JsPath \ JSON_CREATOR_USER_ID).write[Long] and
      (JsPath \ JSON_CATEGORY_ID).write[Long] and
      (JsPath \ JSON_AMOUNT).write[Int]


  /**
   * Default JSON reader for a new Expense object with no ID or create date.
   */
  val NewJsonReader =
    (JsonReaderBase ~ (JsPath \ JSON_CREATOR_USER_ID).read[Long])
        .apply { (loc, desc, pid, ecid, am, cid) => Expense(loc, desc, pid, cid, ecid, am) }

  val NewJsonWriter = JsonWriterBase.apply(unlift(Expense.unapply))

  /**
   * JSON writer for an Expense object that has an ID and create date.
   */
  val InsertedJsonWriter = ((JsPath \ JSON_EXPENSE_ID).write[Long] and
      (JsPath \ JSON_CREATE_DATE).write[DateTime] and
      NewJsonWriter).apply(unlift(InsertedExpense.unapply))


  def insertedFullJsonWriter(participants: Seq[InsertedUserExpenseJoin]) = {
    implicit val insertedUserExpenseJW = UserExpenseJoin.InsertedJsonWriter
    (InsertedJsonWriter ~ (JsPath \ JSON_PARTICIPANTS)
        .write[Seq[InsertedUserExpenseJoin]])
        .apply { exp: InsertedExpense => (exp, participants) }
  }

  def jsonReaderSetUserId(userId: Long) =
    JsonReaderBase.apply { (loc, desc, pid, ecid, am) => Expense(loc, desc, pid, userId, ecid, am) }
}

object ExpenseOperator {
  def createExpense(newList: Expense, participantProvider: Long => Seq[UserExpenseJoin]):
      Either[(InsertedExpense, Seq[InsertedUserExpenseJoin]), ErrorType] = {

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
            Left((insertedList, insParts))
          case Right(error) =>
            Right(error)
        }
      case Right(error) =>
        Right(error)
    }
  }
}
