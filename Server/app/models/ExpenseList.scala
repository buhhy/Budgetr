package models

import org.joda.time.DateTime
import play.api.libs.json.{Json, JsObject, JsPath}
import play.api.libs.functional.syntax._

case class ExpenseList(
    expListId: Option[Long], creatorId: Long,
    name: String, desc: String, createDate: Option[DateTime]) {

  implicit val ejw = Expense.JsonWriter

  def toJson(expenses: Seq[Expense]): JsObject =
    ExpenseList.JsonWriter.writes(this) ++ Json.obj("expenses" -> Json.toJson(expenses))
}

object ExpenseList {
  def buildJsonReader(userId: Long) =
    ((JsPath \ "expenseListId").readNullable[Long] and
        (JsPath \ "name").read[String] and
        (JsPath \ "description").read[String]).apply { (id, name, desc) =>
      ExpenseList(id, userId, name, desc, None)
    }

  val JsonWriter = ((JsPath \ "expenseListId").writeNullable[Long] and
      (JsPath \ "creatorId").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "createDate").writeNullable[DateTime]).apply {
    unlift(ExpenseList.unapply)
  }
}
