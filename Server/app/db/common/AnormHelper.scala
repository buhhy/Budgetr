package db.common

import anorm.{ResultSetParser, SqlParser, NamedParameter, SQL}
import controllers.common.{MultiError, ErrorType, DBError}
import org.joda.time.DateTime
import play.api.Logger
import play.api.db.DB
import play.api.Play.current

object AnormHelper {
  private[db] val SingleIdParser = SqlParser.scalar[Long].single

  // for join tables that might not have auto-generated ID columns
  private[db] val SingleIdOptParser = SqlParser.scalar[Long].singleOpt

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

object AnormInsertHelper {
  def apply(tableName: String, createDateColumn: String) =
      new AnormInsertHelper[Long](tableName, createDateColumn, AnormHelper.SingleIdParser)
}

class AnormInsertHelper[ResultType](
    val tableName: String, val createDateColumn: String,
    val idColumnParser: ResultSetParser[ResultType]) {

  def insert(
      insertValues: Seq[NamedParameter],
      createDateOpt: Option[DateTime] = None): Either[(ResultType, DateTime), ErrorType] = {

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

  def insertAll(
      insertValues: Seq[Seq[NamedParameter]],
      createDateOpt: Option[DateTime] = None): Either[Seq[(ResultType, DateTime)], ErrorType] = {

    // TODO(tlei): use batch inserting instead of multiple single inserts
    val (insertedIds, errors) = insertValues.map(insert(_, createDateOpt))
        .foldLeft(Seq[(ResultType, DateTime)](), Seq[ErrorType]()) { case ((ids, errs), cur) =>
          cur.fold(l => (ids :+ l, errs), r => (ids, errs :+ r))
        }

    if (insertedIds.isEmpty)
      Left(insertedIds)
    else
      Right(MultiError(errors))
//    val createDate = createDateOpt.getOrElse(new DateTime())
//
//    // Remove the create date key in each of the insert sets, and append the index of the set to the
//    // named parameter key value so we can use a prepared statement with unique keys.
//    val newInsertValues = insertValues.zipWithIndex.map { case (ivs, index) =>
//      ivs.map { iv =>
//        if (iv.name.equals(createDateColumn))
//          NamedParameter(s"${createDateColumn}_$index", createDate)
//        else
//          NamedParameter(s"${iv.name}_$index", iv.value)
//      }
//    }.flatten
//
//
//    val allColumnNames = newInsertValues.map(_.name).mkString(", ")
//    val allColumnValues = newInsertValues.map(_.name).map(AnormHelper.replaceStr).mkString(", ")
//
//    DB.withConnection { implicit conn =>
//      AnormHelper.runSql {
//        Left(SQL(
//          s"""
//            |INSERT INTO $tableName($allColumnNames)
//            |VALUES($allColumnValues);
//          """.stripMargin).on(newInsertValues: _*).executeInsert(idColumnParser))
//      }.fold(id => Left(id, createDate), Right(_))
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
