package models

import org.joda.time.DateTime
import play.api.libs.json.JsPath
import play.api.libs.functional.syntax._

case class ExpenseCategory(name: String, creatorId: Long, parentListId: Long)

case class InsertedExpenseCategory(expCatId: Long, createDate: DateTime, expCat: ExpenseCategory)

object ExpenseCategory {
  private val JsonReaderBase = (JsPath \ "name").read[String] and
      (JsPath \ "parentListId").read[Long]

  val JsonWriter = ((JsPath \ "name").write[String] and
      (JsPath \ "creatorId").write[Long] and
      (JsPath \ "parentListId").write[Long]).apply(unlift(ExpenseCategory.unapply))

  val InsertedJsonWriter = ((JsPath \ "expenseCategoryId").write[Long] and
      (JsPath \ "createDate").write[DateTime] and
      JsonWriter).apply(unlift(InsertedExpenseCategory.unapply))

  def jsonReaderFromUserId(userId: Long) =
    JsonReaderBase.apply { (name, pid) =>
      ExpenseCategory(name, userId, pid)
    }
}
