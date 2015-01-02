package db

import anorm.NamedParameter
import models.Expense

object DBExpense {
  private val helper = new AnormHelper("expense")
  private val idColumn = 'exp_id

  def toData(exp: Expense, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      'location -> exp.location,
      'description -> exp.desc,
      'parent_id -> exp.parent.expListId,
      'creator_id -> exp.creator.userId,
      'amount -> exp.amount,
      'input_date -> exp.inputDate)

    if (withId)
      values :+ new NamedParameter(idColumn.name, exp.expId)
    else
      values
  }

  def save(exp: Expense) = helper.insert(toData(exp, withId = true))
  def update(exp: Expense) = helper.update(toData(exp, withId = false), Seq(idColumn -> exp.expId))
  def delete(id: Long) = helper.delete(Seq(idColumn -> id))
}