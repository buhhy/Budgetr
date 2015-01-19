package db

import anorm.SqlParser._
import anorm.{NamedParameter, _}
import controllers.common.{AuthenticationError, DBError, ErrorType}
import models.User
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
  private val helper = new AnormHelper(TableName, Some(C_CDate))

  val UserParser =
    (long(C_ID) ~ str(C_Phone) ~ str(C_Email) ~ str(C_Password) ~ date(C_CDate)).map {
      case id ~ phone ~ email ~ pass ~ date =>
        User(Some(id), phone, email, pass, Some(new DateTime(date)))
    }



  def toData(user: User, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      C_Phone -> user.phone,
      C_Email -> user.email,
      C_Password -> user.password,
      C_CDate -> user.createDate)

    if (withId)
      values ++ idColumns(user.userId)
    else
      values
  }

  def idColumns(id: Option[Long]): Seq[NamedParameter] =
    id.collect { case n => NamedParameter(C_ID, n)}.toSeq

  def insert(user: User): Either[User, ErrorType] = {
    helper.insert(toData(user, withId = true), user.createDate).fold(
      id => Left(user.copy(userId = Some(id._1), createDate = id._2)),
      err => Right(err))
  }

  def update(user: User) =
    helper.update(toData(user, withId = false), idColumns(user.userId))

  def delete(id: Long) = helper.delete(idColumns(Some(id)))

  def find(id: Long): Either[User, ErrorType] = {
    DB.withConnection { implicit conn =>
      helper.runSql {
        anorm.SQL(
          s"""
              |SELECT * FROM $TableName WHERE $C_ID = {$C_ID}
            """.stripMargin)
            .on(idColumns(Some(id)): _*).as(UserParser.singleOpt)
            .map(Left(_)).getOrElse(Right(DBError(s"Could not find user with id `$id`.")))
      }
    }
  }

  def authenticate(phone: String, password: String): Either[User, ErrorType] = {
    DB.withConnection { implicit conn =>
      helper.runSql {
        anorm.SQL(
          s"""
            |SELECT * FROM $TableName
            |WHERE $TableName.$C_Phone = {$C_Phone}
            |  AND $TableName.$C_Password = {$C_Password}
          """.stripMargin
        ).on(C_Phone -> phone, C_Password -> password).as(UserParser.singleOpt)
            .map(Left(_)).getOrElse(
              Right(AuthenticationError("Could not login with the given username and password.")))
      }
    }
  }
}