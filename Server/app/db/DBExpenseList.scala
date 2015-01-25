package db

import anorm.SqlParser._
import anorm.{NamedParameter, ~}
import controllers.common.{DBError, ErrorType}
import models.{ExpenseList, InsertedExpense, InsertedExpenseList}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.DB

object DBExpenseList {
  private val TableName = "expense_list"
  private val C_ID = "explist_id"
  private val C_CID = "creator_id"
  private val C_Name = "name"
  private val C_Desc = "description"
  private val C_CDate = "create_date"

  private val helper = new AnormHelper(TableName)
  private val insertHelper = AnormInsertHelper(TableName, C_CDate)

  val ExpenseListParser =
    (long(C_ID) ~ long(C_CID) ~ str(C_Name) ~ str(C_Desc) ~ date(C_CDate)).map {
      case id ~ cid ~ name ~ desc ~ date =>
        InsertedExpenseList(id, new DateTime(date), ExpenseList(cid, name, desc))
    }

  def toData(explist: ExpenseList): Seq[NamedParameter] = {
    Seq(
      C_CID -> explist.creatorId,
      C_Name -> explist.name,
      C_Desc -> explist.desc)
  }

  def toData(explist: InsertedExpenseList): Seq[NamedParameter] = {
    toData(explist.expenseList) ++ Seq[NamedParameter](
      idColumn(explist.expListId),
      C_CDate -> explist.createDate)
  }

  def idColumn(id: Long): NamedParameter = NamedParameter(C_ID, id)

  def insert(explist: ExpenseList): Either[InsertedExpenseList, ErrorType] =
    insert(None, explist)

  def insert(id: Long, explist: ExpenseList): Either[InsertedExpenseList, ErrorType] =
    insert(Some(id), explist)

  def insert(id: Option[Long], explist: ExpenseList): Either[InsertedExpenseList, ErrorType] = {
    insertHelper.insert(toData(explist) ++ id.map(idColumn), None).fold(
      id => Left(InsertedExpenseList(id._1, id._2, explist)),
      err => Right(err))
  }

  def update(id: Long, explist: ExpenseList) =
    helper.update(toData(explist), Seq(idColumn(id)))

  def delete(id: Long) = helper.delete(Seq(idColumn(id)))

  def find(id: Long): Either[InsertedExpenseList, ErrorType] = {
    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        anorm.SQL(
          s"""
             |SELECT * FROM $TableName WHERE $C_ID = ${AnormHelper.replaceStr(C_ID)}
           """.stripMargin)
            .on(idColumn(id))
            .as(ExpenseListParser.singleOpt)
            .map(Left(_))
            .getOrElse(Right(DBError(s"Could not find an expense list with the given id `$id`.")))
      }
    }
  }

  def findAssociatedExpenses(id: Long): Either[Seq[InsertedExpense], ErrorType] = {
    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        Left(anorm.SQL(
          s"""
             |SELECT * FROM ${DBExpense.TableName}
             |  WHERE ${DBExpense.C_PID} = ${AnormHelper.replaceStr(C_ID)}
           """.stripMargin)
            .on(idColumn(id))
            .as(DBExpense.ExpenseParser.*))
      }
    }
  }
}
