package models

import org.joda.time.DateTime
import play.api.libs.json.JsPath
import play.api.libs.functional.syntax._

/**
 * @param paidAmount [percent]
 * @param responsibleAmount [percent]
 */
case class UserExpenseJoin(
    userId: Long, expenseId: Long, paidAmount: Double, responsibleAmount: Double)

case class InsertedUserExpenseJoin(createDate: DateTime, join: UserExpenseJoin)

object UserExpenseJoin {
  val JsonReader = ((JsPath \ "userId").read[Long] and
      (JsPath \ "expenseId").read[Long] and
      (JsPath \ "paidAmount").read[Double] and
      (JsPath \ "responsibleAmount").read[Double]).apply(UserExpenseJoin)

  val JsonWriter = ((JsPath \ "userId").write[Long] and
      (JsPath \ "expenseId").write[Long] and
      (JsPath \ "paidAmount").write[Double] and
      (JsPath \ "responsibleAmount").write[Double]).apply(unlift(UserExpenseJoin.unapply))

  val InsertedJsonWriter = ((JsPath \ "createDate").write[DateTime] and JsonWriter)
      .apply(unlift(InsertedUserExpenseJoin.unapply))
}