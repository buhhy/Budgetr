package db

import anorm.{ResultSetParser, SqlParser, NamedParameter, SQL}
import controllers.common.{ErrorType, DBError}
import org.joda.time.DateTime
import play.api.Logger
import play.api.db.DB
import play.api.Play.current

object AnormHelper {
  private[db] val SingleIdParser = SqlParser.scalar[Long].single

  def replaceStr(str: String) = s"{$str}"

  def namedParametersToEqualsStrings(nps: Seq[NamedParameter]) =
    nps.map { n => s"${n.name} = ${replaceStr(n.name)}"}

  def runSql[T](func: => Either[T, ErrorType]): Either[T, ErrorType] = {
    try {
      func
    } catch {
      case e: Exception =>
        e.printStackTrace()
        Logger.logger.error("SQL Error: " + e.getMessage)
        Right(DBError("Oops, a database error occurred."))
    }
  }
}

class AnormInsertHelper(val tableName: String, val createDateColumn: String) {
  def insert(insertValues: Seq[NamedParameter]): Either[(Long, DateTime), ErrorType] =
    insert(insertValues, AnormHelper.SingleIdParser, None)

  def insert(
      insertValues: Seq[NamedParameter],
      createDateOpt: Option[DateTime]): Either[(Long, DateTime), ErrorType] =
    insert[Long](insertValues, AnormHelper.SingleIdParser, createDateOpt)

  def insert[ResultType](
      insertValues: Seq[NamedParameter],
      idColumnParser: ResultSetParser[ResultType],
      createDateOpt: Option[DateTime]): Either[(ResultType, DateTime), ErrorType] = {

    // TODO(tlei): use UTC time zone
    val createDate = createDateOpt.getOrElse(new DateTime())

    // Remove the create date key provided in insertValues and add the newly instantiated
    // date value instead.
    val newInsertValues = insertValues.filterNot { v => v.name.equals(createDateColumn)} :+
        NamedParameter(createDateColumn, createDate)

    val columnNames = newInsertValues.map(_.name).mkString(", ")
    val columnValues = newInsertValues.map(_.name).map(AnormHelper.replaceStr).mkString(", ")

    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        Left(SQL(
          s"""
            |INSERT INTO $tableName($columnNames)
            |VALUES($columnValues);
          """.stripMargin).on(newInsertValues: _*).executeInsert(idColumnParser))
      }.fold(id => Left(id, createDate), Right(_))
    }
  }
}

class AnormHelper(val tableName: String) {
  def update(
      updatedValues: Seq[NamedParameter],
      conditions: Seq[NamedParameter]): Either[Int, ErrorType] = {
    val setColumns = AnormHelper.namedParametersToEqualsStrings(updatedValues).mkString(", ")
    val idColumns = AnormHelper.namedParametersToEqualsStrings(conditions).mkString(" AND ")

    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        Left(
          SQL(
            s"""
              |UPDATE $tableName
              |SET $setColumns
              |WHERE $idColumns;
            """.stripMargin).on(updatedValues: _*).executeUpdate())
      }
    }
  }

  def delete(conditions: Seq[NamedParameter]): Either[Int, ErrorType] = {
    val idColumns = AnormHelper.namedParametersToEqualsStrings(conditions).mkString(" AND ")

    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        Left(SQL(
          s"""
            DELETE FROM $tableName WHERE $idColumns
          """
        ).on(conditions: _*).executeUpdate())
      }
    }
  }
}
