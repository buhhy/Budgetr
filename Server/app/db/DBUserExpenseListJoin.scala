package db

import anorm.{~, NamedParameter}
import anorm.SqlParser._
import controllers.common.ErrorType
import models.{ExpenseList, InsertedExpenseList, InsertedUserExpenseListJoin, UserExpenseListJoin}
import db.common.{AnormInsertHelper, AnormHelper}
import org.joda.time.DateTime

object DBUserExpenseListJoin {
  private[db] val TableName = "user_expense_list_join"
  private[db] val C_UID = "user_ref_id"
  private[db] val C_EID = "explist_ref_id"
  private[db] val C_CDate = "create_date"

  private[db] val helper = new AnormHelper(TableName)
  private val insertHelper =
    new AnormInsertHelper[Any](TableName, C_CDate, AnormHelper.SingleIdOptParser)

  val UserExpenseListJoinParser =
    (long(C_UID) ~ long(C_EID) ~ date(C_CDate)).map {
      case eid ~ uid ~ date =>
        InsertedUserExpenseListJoin(new DateTime(date), UserExpenseListJoin(eid, uid))
    }

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
  def truncate = helper.truncate
}