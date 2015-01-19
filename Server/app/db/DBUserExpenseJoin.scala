package db

import anorm.SqlParser._
import anorm.{NamedParameter, _}
import controllers.common.ErrorType
import models.UserExpenseJoin

object DBUserExpenseJoin {
  private val C_UID = "user_id"
  private val C_EID = "explist_id"
  private val C_CDATE = "create_date"
  private val helper = new AnormHelper("user_expense_join", Some(C_CDATE))

  import db.DBUserExpenseJoin.helper.s2s

  private val IdParser = (long(C_UID) ~ long(C_EID)).map { case uid ~ eid => (uid, eid) }.single



  def toData(uej: UserExpenseJoin, withId: Boolean = false) = {
    val values: Seq[NamedParameter] = Seq(C_CDATE -> uej.createDate)

    if (withId)
      idColumns(uej) ++ values
    else
      values
  }

  def idColumns(uej: UserExpenseJoin) =
    Seq[NamedParameter](C_UID -> uej.userId, C_EID -> uej.expenseListId)

  def save(uej: UserExpenseJoin): Either[UserExpenseJoin, ErrorType] = {
    helper.insert[(Long, Long)](toData(uej, withId = true), IdParser, uej.createDate).fold(
      id => Left(uej.copy(userId = id._1._1, expenseListId = id._1._2)),
      err => Right(err))
  }

  def update(uej: UserExpenseJoin) = helper.update(toData(uej, withId = false), idColumns(uej))
  def delete(uej: UserExpenseJoin) = helper.delete(idColumns(uej))
}