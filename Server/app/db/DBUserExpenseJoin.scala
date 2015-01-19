package db

import anorm.SqlParser._
import anorm.{NamedParameter, _}
import controllers.common.ErrorType
import models.{InsertedUserExpenseJoin, UserExpenseJoin}

object DBUserExpenseJoin {
  private val C_UID = "user_id"
  private val C_EID = "explist_id"
  private val C_CDate = "create_date"
  private val helper = new AnormHelper("user_expense_join")
  private val insertHelper = new AnormInsertHelper("user_expense_join", C_CDate)

  private val IdParser = (long(C_UID) ~ long(C_EID)).map { case uid ~ eid => (uid, eid)}.singleOpt

  def toData(uej: UserExpenseJoin): Seq[NamedParameter] = idColumns(uej)

  def toData(uej: InsertedUserExpenseJoin) = {
    toData(uej.join) :+ NamedParameter(C_CDate, uej.createDate)
  }

  def idColumns(uej: UserExpenseJoin) =
    Seq[NamedParameter](C_UID -> uej.userId, C_EID -> uej.expenseListId)

  def insert(uej: UserExpenseJoin): Either[InsertedUserExpenseJoin, ErrorType] = {
    // TODO(tlei): inserting isn't going to actually have any row results, since no fields are
    // being auto-generated and are provided instead
    insertHelper.insert[Option[(Long, Long)]](toData(uej), IdParser, None)
        .fold(ret => Left(InsertedUserExpenseJoin(ret._2, uej)), err => Right(err))
  }

  def update(uej: UserExpenseJoin) = helper.update(toData(uej), idColumns(uej))

  def delete(uej: UserExpenseJoin) = helper.delete(idColumns(uej))
}