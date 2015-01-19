package db

import anorm.SqlParser._
import anorm.{NamedParameter, ~}
import controllers.common.ErrorType
import models.Expense
import org.joda.time.DateTime

object DBExpense {
  private[db] val TableName = "expense"
  private[db] val C_ID = "exp_id"
  private[db] val C_LOC = "location"
  private[db] val C_DESC = "description"
  private[db] val C_PID = "parent_id"
  private[db] val C_CID = "creator_id"
  private[db] val C_AMT = "amount"
  private[db] val C_CDATE = "create_date"
  private[db] val helper = new AnormHelper(TableName, Some(C_CDATE))

  val ExpenseParser =
    (long(C_ID) ~ str(C_LOC) ~ str(C_DESC) ~ long(C_PID) ~
        long(C_CID) ~ int(C_AMT) ~ date(C_CDATE)).map {
      case id ~ loc ~ desc ~ pid ~ cid ~ amt ~ date =>
        Expense(Some(id), loc, desc, pid, cid, amt, Some(new DateTime(date)))
    }

  def toData(exp: Expense, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      C_LOC -> exp.location,
      C_DESC -> exp.desc,
      C_PID -> exp.parentListId,
      C_CID -> exp.creatorId,
      C_AMT -> exp.amount,
      C_CDATE -> exp.createDate)

    if (withId)
      values ++ idColumns(exp.expId)
    else
      values
  }

  def idColumns(id: Option[Long]): Seq[NamedParameter] =
    id.collect { case n => NamedParameter(C_ID, n) }.toSeq


  def save(exp: Expense): Either[Expense, ErrorType] = {
    helper.insert(toData(exp, withId = true), exp.createDate).fold(
      id => Left(exp.copy(expId = Some(id._1), createDate = id._2)),
      err => Right(err))
  }

  def update(exp: Expense) = helper.update(toData(exp, withId = false), idColumns(exp.expId))
  def delete(id: Long) = helper.delete(idColumns(Some(id)))
}