package db

import anorm._
import play.api.db.DB
import play.api.Play.current

class AnormHelper(val tableName: String) {
  private def namedParametersToEqualsStrings(nps: Seq[NamedParameter]) =
    nps.map { n => s"${n.name} = {${n.name}}" }

  def insert(insertValues: Seq[NamedParameter]): Option[Long] = {
    val columnNames = insertValues.map(_.name).mkString(", ")
    val columnValues = namedParametersToEqualsStrings(insertValues).mkString(", ")

    DB.withConnection { implicit conn =>
      try {
        SQL(
          s"""
            INSERT INTO $tableName($columnNames)
            VALUES($columnValues);
          """
        ).on(insertValues: _*).executeInsert()
      } catch {
        case e: Exception =>
          println(e.getMessage)
          None
      }
    }
  }

  def update(updatedValues: Seq[NamedParameter], conditions: Seq[NamedParameter]): Int = {
    val setColumns = namedParametersToEqualsStrings(updatedValues).mkString(", ")
    val idColumns = namedParametersToEqualsStrings(conditions).mkString(" AND ")

    DB.withConnection { implicit conn =>
      SQL(
        s"""
          UPDATE $tableName
          SET $setColumns
          WHERE $idColumns;
        """
      ).on(updatedValues: _*).executeUpdate()
    }
  }

  def delete(conditions: Seq[NamedParameter]): Int = {
    val idColumns = namedParametersToEqualsStrings(conditions).mkString(" AND ")

    DB.withConnection { implicit conn =>
      SQL(
        s"""
          DELETE FROM $tableName WHERE $idColumns
        """
      ).on(conditions: _*).executeUpdate()
    }
  }
}
