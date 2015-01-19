package models

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsPath}
import play.api.libs.functional.syntax._

case class Expense(
    expId: Option[Long], location: String, desc: String,
    parentListId: Long, creatorId: Long, amount: Int, createDate: Option[DateTime]) {
  def toJson: JsObject = Expense.JsonWriter.writes(this)
}

object Expense {
  def buildJsonReader(userId: Long) =
    ((JsPath \ "expenseId").readNullable[Long] and
        (JsPath \ "location").read[String] and
        (JsPath \ "description").read[String] and
        (JsPath \ "parentId").read[Long] and
        (JsPath \ "amount").read[Int]).apply { (id, loc, desc, pid, am) =>
      Expense(id, loc, desc, pid, userId, am, None)
    }

  val JsonWriter = ((JsPath \ "expenseId").writeNullable[Long] and
      (JsPath \ "location").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "parentId").write[Long] and
      (JsPath \ "creatorId").write[Long] and
      (JsPath \ "amount").write[Int] and
      (JsPath \ "createDate").writeNullable[DateTime]).apply(unlift(Expense.unapply))
}
