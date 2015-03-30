package db

import anorm.NamedParameter
import controllers.common.ErrorType
import models.{InsertedUserExpenseJoin, UserExpenseJoin}
import db.common.{AnormInsertHelper, AnormHelper}

object DBUserExpenseJoin {
  private[db] val TableName = "user_expense_join"
  private[db] val C_UID = "user_ref_id"
  private[db] val C_EID = "exp_ref_id"
  private[db] val C_PAMOUNT = "paid_amount"
  private[db] val C_RAMOUNT = "responsible_amount"
  private[db] val C_CDate = "create_date"
  private[db] val helper = new AnormHelper(TableName)
  private val insertHelper =
    new AnormInsertHelper[Any](TableName, C_CDate, AnormHelper.SingleIdOptParser)

  def toData(uej: UserExpenseJoin): Seq[NamedParameter] =
    idColumns(uej) ++ Seq[NamedParameter](
      C_PAMOUNT -> uej.paidAmount,
      C_RAMOUNT -> uej.responsibleAmount)

  def toData(uej: InsertedUserExpenseJoin): Seq[NamedParameter] =
    toData(uej.join) :+ NamedParameter(C_CDate, uej.createDate)

  def idColumns(uej: UserExpenseJoin) =
    Seq[NamedParameter](C_UID -> uej.userId, C_EID -> uej.expenseId)

  def insert(uej: UserExpenseJoin): Either[InsertedUserExpenseJoin, ErrorType] = {
    insertHelper.insert(toData(uej), None)
        .fold(ret => Left(InsertedUserExpenseJoin(ret._2, uej)), err => Right(err))
  }

  def insertAll(uejs: Seq[UserExpenseJoin]): Either[Seq[InsertedUserExpenseJoin], ErrorType] = {
    insertHelper.insertAll(uejs.map(toData), None)
        .fold(rets => {
          Left(rets.zip(uejs).map { case ((_, date), uej) =>
            InsertedUserExpenseJoin(date, uej)
          })
        }, err => Right(err))
  }

  def update(uej: UserExpenseJoin) = helper.update(toData(uej), idColumns(uej))

  def delete(uej: UserExpenseJoin) = helper.delete(idColumns(uej))
  def truncate() = helper.truncate()
}