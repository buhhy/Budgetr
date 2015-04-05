package models

import org.joda.time.DateTime
import play.api.libs.json.{Json, JsObject, JsPath}
import play.api.libs.functional.syntax._

case class ExpenseList(creatorId: Long, name: String, desc: String) {
  def toJson: JsObject = ExpenseList.NewJsonWriter.writes(this)
}

case class InsertedExpenseList(expListId: Long, createDate: DateTime, expenseList: ExpenseList) {
  private implicit val expenseJW = ExpenseJson.InsertedJsonWriter
  private implicit val expenseCategoryJW = ExpenseCategory.InsertedJsonWriter
  private implicit val userExpenseListJoinJW = InsertedUserExpenseListJoinWithUser.JsonWriter

  def toJson(
      expenses: Seq[InsertedExpense],
      categories: Seq[InsertedExpenseCategory],
      members: Seq[InsertedUserExpenseListJoinWithUser]): JsObject = {

    InsertedExpenseList.JsonWriter.writes(this) ++
        Json.obj("expenses" -> Json.toJson(expenses)) ++
        Json.obj("categories" -> Json.toJson(categories)) ++
        Json.obj("members" -> Json.toJson(members))
  }
}

object ExpenseList {
  private val JsonWriterBase = (JsPath \ "creatorId").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String]

  val NewJsonWriter = JsonWriterBase.apply(unlift(ExpenseList.unapply))

  def jsonReaderFromUserId(userId: Long) = ((JsPath \ "name").read[String] and
        (JsPath \ "description").read[String]).apply { (name, desc) =>
      ExpenseList(userId, name, desc)
    }
}

object InsertedExpenseList {
  val JsonWriter = ((JsPath \ "expenseListId").write[Long] and
      (JsPath \ "createDate").write[DateTime] and
      ExpenseList.NewJsonWriter).apply(unlift(InsertedExpenseList.unapply))
}