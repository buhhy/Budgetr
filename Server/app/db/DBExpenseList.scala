package db

import anorm.NamedParameter
import models.ExpenseList

object DBExpenseList {
  private val C_ID = 'explist_id
  private val C_CID = 'phone
  private val C_NAME = 'email
  private val C_DESC = 'password
  private val C_CDATE = 'registration_date
  private val helper = new AnormHelper("expense_list")

  import helper.symbolToString

  def toData(explist: ExpenseList, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      C_CID -> explist.creator.userId,
      C_NAME -> explist.name,
      C_DESC -> explist.desc,
      C_CDATE -> explist.createDate)

    if (withId)
      values ++ idColumns(explist.expListId)
    else
      values
  }

  def idColumns(id: Option[Long]): Seq[NamedParameter] =
    id.collect { case n => NamedParameter(C_ID, n) }.toSeq

//  def save(explist: ExpenseList) = helper.insert(toData(explist, withId = true))
  def update(explist: ExpenseList) =
    helper.update(toData(explist, withId = false), idColumns(explist.expListId))
  def delete(id: Long) = helper.delete(idColumns(Some(id)))
}
