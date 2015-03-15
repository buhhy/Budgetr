package db

import anorm.SqlParser._
import anorm.{NamedParameter, ~}
import controllers.common.ErrorType
import db.common.{AnormHelper, AnormInsertHelper}
import models.{ExpenseCategory, InsertedExpenseCategory}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.DB

object DBExpenseCategory {
  private[db] val TableName = "expense_category"
  private[db] val C_ID = "expcat_id"
  private[db] val C_Name = "cat_name"
  private[db] val C_CDate = "create_date"
  private[db] val C_CID = "creator_ref_id"
  private[db] val C_ELID = "explist_ref_id"

  private val helper = new AnormHelper(TableName)
  private val insertHelper = AnormInsertHelper(TableName, C_CDate)

  val ExpenseCategoryParser =
    (long(C_ID) ~ str(C_Name) ~ long(C_CID) ~ long(C_ELID) ~ date(C_CDate)).map {
      case id ~ name ~ cid ~ elid ~ date =>
        InsertedExpenseCategory(id, new DateTime(date), ExpenseCategory(name, cid, elid))
    }

  def idColumn(id: Long): NamedParameter = NamedParameter(C_ID, id)

  def toData(expCat: ExpenseCategory): Seq[NamedParameter] =
    Seq(
      C_Name -> expCat.name,
      C_CID -> expCat.creatorId,
      C_ELID -> expCat.parentListId)

  def find(name: String, parentListId: Long): Either[Option[InsertedExpenseCategory], ErrorType] = {
    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        anorm.SQL(
          s"""
             |SELECT * FROM $TableName
             |  WHERE $C_ELID = ${AnormHelper.replaceStr(C_ELID)}
             |    AND $C_Name = ${AnormHelper.replaceStr(C_Name)}
           """.stripMargin)
            .on(C_ELID -> parentListId, C_Name -> name)
            .as(ExpenseCategoryParser.singleOpt)
            .map(e => Left(Some(e)))
            .getOrElse(Left(None))
      }
    }
  }

  def insert(expCat: ExpenseCategory): Either[InsertedExpenseCategory, ErrorType] =
    insert(None, expCat)

  def insert(id: Long, expCat: ExpenseCategory): Either[InsertedExpenseCategory, ErrorType] =
    insert(Some(id), expCat)

  private def insert(id: Option[Long], expCat: ExpenseCategory):
      Either[InsertedExpenseCategory, ErrorType] = {

    insertHelper.insert(toData(expCat) ++ id.map(idColumn)).fold(
      id => Left(InsertedExpenseCategory(id._1, id._2, expCat)),
      err => Right(err))
  }

  def update(id: Long, expCat: ExpenseCategory) =
    helper.update(toData(expCat), Seq(idColumn(id)))
  def delete(id: Long) = helper.delete(Seq(idColumn(id)))
  def truncate = helper.truncate
}
