package models

import org.joda.time.DateTime

case class Expense(
  expId: Option[Long], location: String, desc: String,
  parent: ExpenseList, creator: User,
  amount: Int, inputDate: DateTime)
