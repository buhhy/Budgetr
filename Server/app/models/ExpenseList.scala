package models

import org.joda.time.DateTime

case class ExpenseList(
  expListId: Option[Long], creator: User,
  name: String, desc: String, createDate: DateTime)
