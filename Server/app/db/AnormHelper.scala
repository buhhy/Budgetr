package db

import anorm.{ResultSetParser, SqlParser, NamedParameter, SQL}
import controllers.common.{ErrorType, DBError}
import org.joda.time.DateTime
import play.api.Logger
import play.api.db.DB
import play.api.Play.current

object AnormHelper {
}

class AnormHelper(val tableName: String, val createDateColumn: Option[String] = None) {
  def replaceStr(str: String) = s"{$str}"
  def namedParametersToEqualsStrings(nps: Seq[NamedParameter]) =
    nps.map { n => s"${n.name} = ${replaceStr(n.name)}"}

  private val SingleIdParser = SqlParser.scalar[Long].single

  implicit def s2s(sym: Symbol): String = sym.name

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

  def insert(insertValues: Seq[NamedParameter]): Either[(Long, Option[DateTime]), ErrorType] =
    insert(insertValues, SingleIdParser, None)

  def insert(
      insertValues: Seq[NamedParameter],
      createDateOpt: Option[DateTime]): Either[(Long, Option[DateTime]), ErrorType] =
    insert[Long](insertValues, SingleIdParser, createDateOpt)

  def insert[ResultType](
      insertValues: Seq[NamedParameter],
      idColumnParser: ResultSetParser[ResultType],
      createDateOpt: Option[DateTime]): Either[(ResultType, Option[DateTime]), ErrorType] = {

    val (newInsertValues, createDate) = createDateColumn match {
      case Some(cdc) =>
        val createDate = createDateOpt.getOrElse(new DateTime())

        // Remove the create date key provided in insertValues and add the newly instantiated
        // date value instead.
        val niv = insertValues.filterNot { v => v.name.equals(cdc)} :+
            NamedParameter(cdc, createDate)
        (niv, Some(createDate))
      case None =>
        (insertValues, None)
    }

    val columnNames = newInsertValues.map(_.name).mkString(", ")
    val columnValues = newInsertValues.map(_.name).map(replaceStr).mkString(", ")

    DB.withConnection { implicit conn =>
      runSql {
        Left(SQL(
          s"""
            |INSERT INTO $tableName($columnNames)
            |VALUES($columnValues);
          """.stripMargin).on(newInsertValues: _*).executeInsert(idColumnParser))
      }.fold(id => Left(id, createDate), Right(_))
    }
  }

  def update(
      updatedValues: Seq[NamedParameter],
      conditions: Seq[NamedParameter]): Either[Int, ErrorType] = {
    val setColumns = namedParametersToEqualsStrings(updatedValues).mkString(", ")
    val idColumns = namedParametersToEqualsStrings(conditions).mkString(" AND ")

    DB.withConnection { implicit conn =>
      runSql {
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
    val idColumns = namedParametersToEqualsStrings(conditions).mkString(" AND ")

    DB.withConnection { implicit conn =>
      runSql {
        Left(SQL(
          s"""
            DELETE FROM $tableName WHERE $idColumns
          """
        ).on(conditions: _*).executeUpdate())
      }
    }
  }
}
