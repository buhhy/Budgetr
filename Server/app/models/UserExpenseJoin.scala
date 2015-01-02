package models

import org.joda.time.DateTime

case class UserExpenseJoin(user: User, expenseList: ExpenseList, joinDate: DateTime)
