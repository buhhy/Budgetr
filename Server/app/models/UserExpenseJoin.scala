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
  val JSON_USER_ID = "userId"
  val JSON_EXPENSE_ID = "expenseId"
  val JSON_PAID_AMOUNT = "paidAmount"
  val JSON_RESPONSIBLE_AMOUNT = "responsibleAmount"

  val JsonReader = ((JsPath \ JSON_USER_ID).read[Long] and
      (JsPath \ JSON_EXPENSE_ID).read[Long] and
      (JsPath \ JSON_PAID_AMOUNT).read[Double] and
      (JsPath \ JSON_RESPONSIBLE_AMOUNT).read[Double]).apply(UserExpenseJoin.apply _)

  val JsonWriter = ((JsPath \ JSON_USER_ID).write[Long] and
      (JsPath \ JSON_EXPENSE_ID).write[Long] and
      (JsPath \ JSON_PAID_AMOUNT).write[Double] and
      (JsPath \ JSON_RESPONSIBLE_AMOUNT).write[Double]).apply(unlift(UserExpenseJoin.unapply))

  val InsertedJsonWriter = ((JsPath \ "createDate").write[DateTime] and JsonWriter)
      .apply(unlift(InsertedUserExpenseJoin.unapply))

  def jsonReaderSetExpenseId(expenseId: Long) = ((JsPath \ JSON_USER_ID).read[Long] and
      (JsPath \ JSON_PAID_AMOUNT).read[Double] and
      (JsPath \ JSON_RESPONSIBLE_AMOUNT).read[Double])
      .apply { (uid, pamt, ramt) => UserExpenseJoin(uid, expenseId, pamt, ramt) }
}