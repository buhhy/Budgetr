package db

import anorm.NamedParameter
import controllers.common.ErrorType
import models.{InsertedUserExpenseJoin, UserExpenseJoin}

object DBUserExpenseJoin {
  private[db] val TableName = "user_expense_join"
  private[db] val C_UID = "user_ref_id"
  private[db] val C_EID = "explist_ref_id"
  private[db] val C_CDate = "create_date"
  private[db] val helper = new AnormHelper(TableName)
  private val insertHelper =
    new AnormInsertHelper[Any](TableName, C_CDate, AnormHelper.SingleIdOptParser)

  def toData(uej: UserExpenseJoin): Seq[NamedParameter] = idColumns(uej)

  def toData(uej: InsertedUserExpenseJoin): Seq[NamedParameter] =
    toData(uej.join) :+ NamedParameter(C_CDate, uej.createDate)

  def idColumns(uej: UserExpenseJoin) =
    Seq[NamedParameter](C_UID -> uej.userId, C_EID -> uej.expenseListId)

  def insert(uej: UserExpenseJoin): Either[InsertedUserExpenseJoin, ErrorType] = {
    insertHelper.insert(toData(uej), None)
        .fold(ret => Left(InsertedUserExpenseJoin(ret._2, uej)), err => Right(err))
  }

  def update(uej: UserExpenseJoin) = helper.update(toData(uej), idColumns(uej))

  def delete(uej: UserExpenseJoin) = helper.delete(idColumns(uej))
}