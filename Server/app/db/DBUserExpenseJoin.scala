package db

import anorm.NamedParameter
import models.UserExpenseJoin

object DBUserExpenseJoin {
  private val helper = new AnormHelper("user_expense_join")
  private val C_UID = 'user_id
  private val C_EID = 'explist_id
  private val C_JDATE = 'join_date

  def toData(uej: UserExpenseJoin, withId: Boolean = false) = {
    val values: Seq[NamedParameter] = Seq(C_JDATE -> uej.joinDate)

    if (withId)
      idColumns(uej) ++ values
    else
      values
  }

  def idColumns(uej: UserExpenseJoin) =
    Seq[NamedParameter](C_UID -> uej.user.userId.get, C_EID -> uej.expenseList.expListId.get)

//  def save(uej: UserExpenseJoin) = helper.insert(toData(uej, withId = true))
  def update(uej: UserExpenseJoin) = helper.update(toData(uej, withId = false), idColumns(uej))
  def delete(uej: UserExpenseJoin) = helper.delete(idColumns(uej))
}