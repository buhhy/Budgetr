package db

import anorm._
import play.api.Logger
import play.api.db.DB
import play.api.Play.current

class AnormHelper(val tableName: String) {
  private def namedParametersToEqualsStrings(nps: Seq[NamedParameter]) =
    nps.map { n => s"${n.name} = {${n.name}}" }

  implicit def symbolToString(sym: Symbol): String = sym.name

  def runSql[T](func:  => Either[T, String]): Either[T, String] = {
    try {
      func
    } catch {
      case e: Exception =>
        e.printStackTrace()
        Logger.logger.error("SQL Error: " + e.getMessage)
        Right("Oops, a database error occurred.")
    }
  }

  def insert(insertValues: Seq[NamedParameter]): Option[String] = {
    val columnNames = insertValues.map(_.name).mkString(", ")
    val columnValues = namedParametersToEqualsStrings(insertValues).mkString(", ")

    DB.withConnection { implicit conn =>
      runSql {
        Left(SQL(
          s"""
            INSERT INTO $tableName($columnNames)
            VALUES($columnValues);
          """
        ).on(insertValues: _*).executeInsert())
      }.fold(_ => None, Some(_))
    }
  }

  def update(
      updatedValues: Seq[NamedParameter],
      conditions: Seq[NamedParameter]): Either[Int, String] = {
    val setColumns = namedParametersToEqualsStrings(updatedValues).mkString(", ")
    val idColumns = namedParametersToEqualsStrings(conditions).mkString(" AND ")

    DB.withConnection { implicit conn =>
      runSql {
        Left(SQL(
          s"""
          UPDATE $tableName
          SET $setColumns
          WHERE $idColumns;
        """
        ).on(updatedValues: _*).executeUpdate())
      }
    }
  }

  def delete(conditions: Seq[NamedParameter]): Either[Int, String] = {
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
