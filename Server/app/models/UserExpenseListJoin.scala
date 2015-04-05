package models

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath

case class UserExpenseListJoin(userId: Long, expenseListId: Long)

case class InsertedUserExpenseListJoin(createDate: DateTime, join: UserExpenseListJoin)

case class InsertedUserExpenseListJoinWithUser(
    join: InsertedUserExpenseListJoin, user: InsertedUser)

object UserExpenseListJoin {
  val JSON_USER_ID = "userId"
  val JSON_EXPENSE_LIST_ID = "expenseListId"
  val JSON_CREATE_DATE = "joinDate"

  val JsonWriter = ((JsPath \ JSON_USER_ID).write[Long]
      and (JsPath \ JSON_EXPENSE_LIST_ID).write[Long]).apply(unlift(UserExpenseListJoin.unapply))
}

object InsertedUserExpenseListJoin {
  import UserExpenseListJoin._

  val JsonWriter = ((JsPath \ JSON_CREATE_DATE).write[DateTime]
      and UserExpenseListJoin.JsonWriter).apply(unlift(InsertedUserExpenseListJoin.unapply))
}

object InsertedUserExpenseListJoinWithUser {
  val JsonWriter = (InsertedUserExpenseListJoin.JsonWriter
      and InsertedUser.JsonWriter).apply(unlift(InsertedUserExpenseListJoinWithUser.unapply))
}