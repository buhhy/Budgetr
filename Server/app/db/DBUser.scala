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
  private val TABLE_NAME = "user"
  private val C_ID = "user_id"
  private val C_PHONE = "phone"
  private val C_EMAIL = "email"
  private val C_PASSWORD = "password"
  private val C_CDATE = "create_date"
  private val helper = new AnormHelper(TABLE_NAME, Some(C_CDATE))

  val UserParser =
    (long(C_ID) ~ str(C_PHONE) ~ str(C_EMAIL) ~ str(C_PASSWORD) ~ date(C_CDATE)).map {
      case id ~ phone ~ email ~ pass ~ date =>
        User(Some(id), phone, email, pass, Some(new DateTime(date)))
    }



  def toData(user: User, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      C_PHONE -> user.phone,
      C_EMAIL -> user.email,
      C_PASSWORD -> user.password,
      C_CDATE -> user.registerDate)

    if (withId)
      values ++ idColumns(user.userId)
    else
      values
  }

  def idColumns(id: Option[Long]): Seq[NamedParameter] =
    id.collect { case n => NamedParameter(C_ID, n)}.toSeq

  def save(user: User): Either[User, ErrorType] = {
    helper.insert(toData(user, withId = true), user.registerDate).fold(
      id => Left(user.copy(userId = Some(id._1), registerDate = id._2)),
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
              |SELECT * FROM $TABLE_NAME WHERE $C_ID = {$C_ID}
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
            |SELECT * FROM $TABLE_NAME
            |WHERE $TABLE_NAME.$C_PHONE = {$C_PHONE}
            |  AND $TABLE_NAME.$C_PASSWORD = {$C_PASSWORD}
          """.stripMargin
        ).on(C_PHONE -> phone, C_PASSWORD -> password).as(UserParser.singleOpt)
            .map(Left(_)).getOrElse(
              Right(AuthenticationError("Could not login with the given username and password.")))
      }
    }
  }
}