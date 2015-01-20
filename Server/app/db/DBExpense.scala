package db

import anorm.SqlParser._
import anorm.{NamedParameter, ~}
import controllers.common.ErrorType
import models.{InsertedExpense, Expense}
import org.joda.time.DateTime

object DBExpense {
  private[db] val TableName = "expense"
  private[db] val C_ID = "exp_id"
  private[db] val C_Loc = "location"
  private[db] val C_Desc = "description"
  private[db] val C_PID = "parent_id"
  private[db] val C_CID = "creator_id"
  private[db] val C_Amt = "amount"
  private[db] val C_CDate = "create_date"

  private val helper = new AnormHelper(TableName)
  private val insertHelper = AnormInsertHelper(TableName, C_CDate)

  val ExpenseParser =
    (long(C_ID) ~ str(C_Loc) ~ str(C_Desc) ~ long(C_PID) ~
        long(C_CID) ~ int(C_Amt) ~ date(C_CDate)).map {
      case id ~ loc ~ desc ~ pid ~ cid ~ amt ~ date =>
        InsertedExpense(id, new DateTime(date), Expense(loc, desc, pid, cid, amt))
    }

  def toData(exp: Expense): Seq[NamedParameter] = {
    Seq(
      C_Loc -> exp.location,
      C_Desc -> exp.desc,
      C_PID -> exp.parentListId,
      C_CID -> exp.creatorId,
      C_Amt -> exp.amount)
  }

  def toData(exp: InsertedExpense): Seq[NamedParameter] =
    toData(exp.expense) ++ Seq[NamedParameter](idColumn(exp.expId), C_CDate -> exp.createDate)

  def idColumn(id: Long): NamedParameter = NamedParameter(C_ID, id)


  def insert(exp: Expense): Either[InsertedExpense, ErrorType] = {
    insertHelper.insert(toData(exp), None).fold(
      id => Left(InsertedExpense(id._1, id._2, exp)),
      err => Right(err))
  }

  def update(id: Long, exp: Expense) =
    helper.update(toData(exp), Seq(idColumn(id)))
  def delete(id: Long) = helper.delete(Seq(idColumn(id)))
}