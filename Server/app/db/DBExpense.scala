package db

import anorm.SqlParser._
import anorm.{NamedParameter, ~}
import controllers.common.ErrorType
import models.{InsertedExpense, Expense}
import org.joda.time.DateTime
import db.common.{AnormInsertHelper, AnormHelper}

object DBExpense {
  private[db] val TableName = "expense"
  private[db] val C_ID = "exp_id"
  private[db] val C_Loc = "location"
  private[db] val C_Desc = "description"
  private[db] val C_PID = "parent_ref_id"
  private[db] val C_CID = "creator_ref_id"
  private[db] val C_ECID = "category_ref_id"
  private[db] val C_Amt = "amount"
  private[db] val C_CDate = "create_date"

  private val helper = new AnormHelper(TableName)
  private val insertHelper = AnormInsertHelper(TableName, C_CDate)

  val ExpenseParser =
    (long(C_ID) ~ str(C_Loc) ~ str(C_Desc) ~ long(C_PID) ~
        long(C_CID) ~ long(C_ECID) ~ int(C_Amt) ~ date(C_CDate)).map {
      case id ~ loc ~ desc ~ pid ~ cid ~ ecid ~ amt ~ date =>
        InsertedExpense(id, new DateTime(date), Expense(loc, desc, pid, cid, ecid, amt))
    }

  def toData(exp: Expense): Seq[NamedParameter] =
    Seq(
      C_Loc -> exp.location,
      C_Desc -> exp.desc,
      C_PID -> exp.parentListId,
      C_CID -> exp.creatorUserId,
      C_ECID -> exp.categoryId,
      C_Amt -> exp.amount)

  def toData(exp: InsertedExpense): Seq[NamedParameter] =
    toData(exp.expense) ++ Seq[NamedParameter](idColumn(exp.expId), C_CDate -> exp.createDate)

  def idColumn(id: Long): NamedParameter = NamedParameter(C_ID, id)


  def insert(exp: Expense): Either[InsertedExpense, ErrorType] = insert(exp, None)
  def insert(exp: Expense, createDate: DateTime): Either[InsertedExpense, ErrorType] =
    insert(exp, Some(createDate))

  private def insert(
      exp: Expense, createDate: Option[DateTime]): Either[InsertedExpense, ErrorType] = {

    insertHelper.insert(toData(exp), createDate).fold(
      id => Left(InsertedExpense(id._1, id._2, exp)),
      err => Right(err))
  }

  def update(id: Long, exp: Expense) =
    helper.update(toData(exp), Seq(idColumn(id)))
  def delete(id: Long) = helper.delete(Seq(idColumn(id)))
  def truncate() = helper.truncate()
}