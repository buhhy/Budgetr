package db

import play.api.Play.current
import anorm.SqlParser._
import anorm.{NamedParameter, ~}
import controllers.common.{DBError, ErrorType}
import models.{Expense, ExpenseList}
import org.joda.time.DateTime
import play.api.db.DB

object DBExpenseList {
  private val TABLE_NAME = "expense_list"
  private val C_ID = "explist_id"
  private val C_CID = "creator_id"
  private val C_NAME = "name"
  private val C_DESC = "description"
  private val C_CDATE = "create_date"
  private val helper = new AnormHelper(TABLE_NAME, createDateColumn = Some(C_CDATE))

  val ExpenseListParser =
    (long(C_ID) ~ long(C_CID) ~ str(C_NAME) ~ str(C_DESC) ~ date(C_CDATE)).map {
      case id ~ cid ~ name ~ desc ~ date =>
        ExpenseList(Some(id), cid, name, desc, Some(new DateTime(date)))
    }

  def toData(explist: ExpenseList, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      C_CID -> explist.creatorId,
      C_NAME -> explist.name,
      C_DESC -> explist.desc,
      C_CDATE -> explist.createDate
    )

    if (withId)
      values ++ idColumns(explist.expListId)
    else
      values
  }

  def idColumns(id: Option[Long]): Seq[NamedParameter] =
    id.collect { case n => NamedParameter(C_ID, n)}.toSeq

  def save(explist: ExpenseList): Either[ExpenseList, ErrorType] = {
    helper.insert(toData(explist, withId = true), explist.createDate).fold(
      id => Left(explist.copy(expListId = Some(id._1), createDate = id._2)),
      err => Right(err))
  }

  def update(explist: ExpenseList) =
    helper.update(toData(explist, withId = false), idColumns(explist.expListId))

  def delete(id: Long) = helper.delete(idColumns(Some(id)))

  def find(id: Long): Either[ExpenseList, ErrorType] = {
    DB.withConnection { implicit conn =>
      helper.runSql {
        anorm.SQL(
          s"""
             |SELECT * FROM $TABLE_NAME WHERE $C_ID = ${helper.replaceStr(C_ID)}
           """.stripMargin)
            .on(idColumns(Some(id)): _*)
            .as(ExpenseListParser.singleOpt)
            .map(Left(_))
            .getOrElse(Right(DBError(s"Could not find an expense list with the given id `$id`.")))
      }
    }
  }

  def findAssociatedExpenses(id: Long): Either[Seq[Expense], ErrorType] = {
    DB.withConnection { implicit conn =>
      helper.runSql {
        Left(anorm.SQL(
          s"""
             |SELECT * FROM ${DBExpense.TableName}
             |  WHERE ${DBExpense.C_PID} = ${helper.replaceStr(C_ID)}
           """.stripMargin)
            .on(idColumns(Some(id)): _*)
            .as(DBExpense.ExpenseParser.*))
      }
    }
  }
}
