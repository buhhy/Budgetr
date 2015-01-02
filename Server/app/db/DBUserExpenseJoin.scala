package db

import anorm.NamedParameter
import models.UserExpenseJoin

object DBUserExpenseJoin {
  private val helper = new AnormHelper("user_expense_join")
  private val idColumn1 = 'user_id
  private val idColumn2 = 'explist_id

  def toData(uej: UserExpenseJoin, withId: Boolean = false) = {
    val values: Seq[NamedParameter] = Seq('join_date -> uej.joinDate)

    if (withId)
      idColumns(uej) ++ values
    else
      values
  }

  def idColumns(uej: UserExpenseJoin) =
    Seq[NamedParameter](idColumn1 -> uej.user.userId, idColumn2 -> uej.expenseList.expListId)

  def save(uej: UserExpenseJoin) = helper.insert(toData(uej, withId = true))
  def update(uej: UserExpenseJoin) = helper.update(toData(uej, withId = false), idColumns(uej))
  def delete(uej: UserExpenseJoin) = helper.delete(idColumns(uej))
}