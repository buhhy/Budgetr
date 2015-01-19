package models

import org.joda.time.DateTime

case class UserExpenseJoin(userId: Long, expenseListId: Long, createDate: Option[DateTime])
