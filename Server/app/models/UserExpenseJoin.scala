package models

import org.joda.time.DateTime

case class UserExpenseJoin(userId: Long, expenseListId: Long)

case class InsertedUserExpenseJoin(createDate: DateTime, join: UserExpenseJoin)