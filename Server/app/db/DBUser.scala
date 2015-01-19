package db

import anorm.SqlParser._
import anorm.{NamedParameter, _}
import controllers.common.{AuthenticationError, DBError, ErrorType}
import models.{InsertedUser, User}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.DB

// TODO(tlei): password hashing lol
object DBUser {
  private val TableName = "user"
  private val C_ID = "user_id"
  private val C_Phone = "phone"
  private val C_Email = "email"
  private val C_Password = "password"
  private val C_CDate = "create_date"

  private val helper = new AnormHelper(TableName)
  private val insertHelper = new AnormInsertHelper(TableName, C_CDate)

  val UserParser =
    (long(C_ID) ~ str(C_Phone) ~ str(C_Email) ~ str(C_Password) ~ date(C_CDate)).map {
      case id ~ phone ~ email ~ pass ~ date =>
        InsertedUser(id, new DateTime(date), User(phone, email, pass))
    }



  def toData(user: User): Seq[NamedParameter] = {
    Seq(
      C_Phone -> user.phone,
      C_Email -> user.email,
      C_Password -> user.password)
  }

  def toData(user: InsertedUser): Seq[NamedParameter] = {
    toData(user.user) ++ Seq[NamedParameter](
      idColumn(user.userId),
      C_CDate -> user.createDate)
  }

  def idColumn(id: Long): NamedParameter = NamedParameter(C_ID, id)

  def insert(user: User): Either[InsertedUser, ErrorType] = {
    insertHelper.insert(toData(user), None).fold(
      id => Left(InsertedUser(id._1, id._2, user)),
      err => Right(err))
  }

  def update(id: Long, user: User) =
    helper.update(toData(user), Seq(idColumn(id)))

  def delete(id: Long) = helper.delete(Seq(idColumn(id)))

  def find(id: Long): Either[InsertedUser, ErrorType] = {
    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        anorm.SQL(
          s"""
              |SELECT * FROM $TableName WHERE $C_ID = ${AnormHelper.replaceStr(C_ID)}
            """.stripMargin)
            .on(idColumn(id)).as(UserParser.singleOpt)
            .map(Left(_)).getOrElse(Right(DBError(s"Could not find user with id `$id`.")))
      }
    }
  }

  def authenticate(phone: String, password: String): Either[InsertedUser, ErrorType] = {
    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        anorm.SQL(
          s"""
            |SELECT * FROM $TableName
            |WHERE $TableName.$C_Phone = ${AnormHelper.replaceStr(C_Phone)}
            |  AND $TableName.$C_Password = ${AnormHelper.replaceStr(C_Password)}
          """.stripMargin
        ).on(C_Phone -> phone, C_Password -> password).as(UserParser.singleOpt)
            .map(Left(_)).getOrElse(
              Right(AuthenticationError("Could not login with the given username and password.")))
      }
    }
  }
}