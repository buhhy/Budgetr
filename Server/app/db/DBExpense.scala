package db

import anorm.NamedParameter
import models.Expense

object DBExpense {
  private val C_ID = 'exp_id
  private val C_LOC = 'location
  private val C_DESC = 'description
  private val C_PID = 'parent_id
  private val C_CID = 'creator_id
  private val C_AMT = 'amount
  private val C_CDATE = 'input_date
  private val helper = new AnormHelper("expense")

  import helper.symbolToString

  def toData(exp: Expense, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      C_LOC -> exp.location,
      C_DESC -> exp.desc,
      C_PID -> exp.parent.expListId,
      C_CID -> exp.creator.userId,
      C_AMT -> exp.amount,
      C_CDATE -> exp.inputDate)

    if (withId)
      values ++ idColumns(exp.expId)
    else
      values
  }

  def idColumns(id: Option[Long]): Seq[NamedParameter] =
    id.collect { case n => NamedParameter(C_ID, n) }.toSeq

//  def save(exp: Expense) = helper.insert(toData(exp, withId = true))
  def update(exp: Expense) = helper.update(toData(exp, withId = false), idColumns(exp.expId))
  def delete(id: Long) = helper.delete(idColumns(Some(id)))
}