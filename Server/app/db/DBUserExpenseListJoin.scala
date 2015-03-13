package db

import anorm.NamedParameter
import controllers.common.ErrorType
import models.{InsertedUserExpenseListJoin, UserExpenseListJoin}
import db.common.{AnormInsertHelper, AnormHelper}

object DBUserExpenseListJoin {
  private[db] val TableName = "user_expense_list_join"
  private[db] val C_UID = "user_ref_id"
  private[db] val C_EID = "explist_ref_id"
  private[db] val C_CDate = "create_date"
  private[db] val helper = new AnormHelper(TableName)
  private val insertHelper =
    new AnormInsertHelper[Any](TableName, C_CDate, AnormHelper.SingleIdOptParser)

  def toData(uej: UserExpenseListJoin): Seq[NamedParameter] = idColumns(uej)

  def toData(uej: InsertedUserExpenseListJoin): Seq[NamedParameter] =
    toData(uej.join) :+ NamedParameter(C_CDate, uej.createDate)

  def idColumns(uej: UserExpenseListJoin) =
    Seq[NamedParameter](C_UID -> uej.userId, C_EID -> uej.expenseListId)

  def insert(uej: UserExpenseListJoin): Either[InsertedUserExpenseListJoin, ErrorType] = {
    insertHelper.insert(toData(uej), None)
        .fold(ret => Left(InsertedUserExpenseListJoin(ret._2, uej)), err => Right(err))
  }

  def update(uej: UserExpenseListJoin) = helper.update(toData(uej), idColumns(uej))

  def delete(uej: UserExpenseListJoin) = helper.delete(idColumns(uej))
}