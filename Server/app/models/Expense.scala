package models

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsPath}
import play.api.libs.functional.syntax._

case class Expense(
    location: String, desc: String, parentListId: Long,
    creatorId: Long, categoryId: Long, amount: Int) {
  def toJson: JsObject = Expense.NewJsonWriter.writes(this)
}

case class InsertedExpense(expId: Long, createDate: DateTime, expense: Expense) {
  def toJson: JsObject = Expense.InsertedJsonWriter.writes(this)
}

object Expense {
  private val JsonReaderBase = (JsPath \ "location").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "parentId").read[Long] and
      (JsPath \ "categoryId").read[Long] and
      (JsPath \ "amount").read[Int]

  private val JsonWriterBase = (JsPath \ "location").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "parentId").write[Long] and
      (JsPath \ "creatorId").write[Long] and
      (JsPath \ "categoryId").write[Long] and
      (JsPath \ "amount").write[Int]

  /**
   * Default JSON reader for a new Expense object with no ID or create date.
   */
  val NewJsonReader =
    (JsonReaderBase ~ (JsPath \ "creatorId").read[Long]).apply { (loc, desc, pid, ecid, am, cid) =>
      Expense(loc, desc, pid, cid, ecid, am)
    }

  val NewJsonWriter = JsonWriterBase.apply(unlift(Expense.unapply))

  /**
   * JSON writer for an Expense object that has an ID and create date.
   */
  val InsertedJsonWriter = ((JsPath \ "expenseId").write[Long] and
      (JsPath \ "createDate").write[DateTime] and
      NewJsonWriter).apply(unlift(InsertedExpense.unapply))

  def jsonReaderFromUserId(userId: Long) =
    JsonReaderBase.apply { (loc, desc, pid, ecid, am) =>
      Expense(loc, desc, pid, userId, ecid, am)
    }
}
