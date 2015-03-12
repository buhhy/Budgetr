package models

import org.joda.time.DateTime

case class UserExpenseListJoin(userId: Long, expenseListId: Long)

case class InsertedUserExpenseListJoin(createDate: DateTime, join: UserExpenseListJoin)