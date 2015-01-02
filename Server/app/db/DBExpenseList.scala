package db

import anorm.NamedParameter
import models.ExpenseList

object DBExpenseList {
  private val helper = new AnormHelper("expense_list")
  private val idColumn = 'explist_id

  def toData(explist: ExpenseList, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      'creator_id -> explist.creator.userId,
      'name -> explist.name,
      'description -> explist.desc,
      'create_date -> explist.createDate)

    if (withId)
      values :+ new NamedParameter(idColumn.name, explist.expListId)
    else
      values
  }

  def save(explist: ExpenseList) = helper.insert(toData(explist, withId = true))
  def update(explist: ExpenseList) =
    helper.update(toData(explist, withId = false), Seq(idColumn -> explist.expListId))
  def delete(id: Long) = helper.delete(Seq(idColumn -> id))
}
