package db

import anorm.SqlParser._
import anorm.{NamedParameter, _}
import controllers.common.ErrorType
import models.UserExpenseJoin

object DBUserExpenseJoin {
  private val C_UID = "user_id"
  private val C_EID = "explist_id"
  private val C_CDate = "create_date"
  private val helper = new AnormHelper("user_expense_join", Some(C_CDate))

  private val IdParser = (long(C_UID) ~ long(C_EID)).map { case uid ~ eid => (uid, eid) }.singleOpt



  def toData(uej: UserExpenseJoin, withId: Boolean = false) = {
    val values: Seq[NamedParameter] = Seq(C_CDate -> uej.createDate)

    if (withId)
      idColumns(uej) ++ values
    else
      values
  }

  def idColumns(uej: UserExpenseJoin) =
    Seq[NamedParameter](C_UID -> uej.userId, C_EID -> uej.expenseListId)

  def insert(uej: UserExpenseJoin): Either[UserExpenseJoin, ErrorType] = {
    // TODO(tlei): inserting isn't going to actually have any row results, since no fields are
    // being auto-generated and are provided instead
    helper.insert[Option[(Long, Long)]](toData(uej, withId = true), IdParser, uej.createDate).fold(
      _ => Left(uej),
      err => Right(err))
  }

  def update(uej: UserExpenseJoin) = helper.update(toData(uej, withId = false), idColumns(uej))
  def delete(uej: UserExpenseJoin) = helper.delete(idColumns(uej))
}