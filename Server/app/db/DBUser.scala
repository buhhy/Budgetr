package db

import anorm._
import anorm.NamedParameter
import anorm.SqlParser._
import models.User
import play.api.Play.current
import org.joda.time.DateTime
import play.api.db.DB

// TODO(tlei): password hashing lol
object DBUser {
  private val TABLE_NAME = "expense_list"
  private val C_ID = 'user_id
  private val C_PHONE = 'phone
  private val C_EMAIL = 'email
  private val C_PASSWORD = 'password
  private val C_REGDATE = 'registration_date
  private val helper = new AnormHelper(TABLE_NAME)

  import helper.symbolToString

  def toData(user: User, withId: Boolean = false): Seq[NamedParameter] = {
    val values: Seq[NamedParameter] = Seq(
      C_PHONE -> user.phone,
      C_EMAIL -> user.email,
      C_PASSWORD -> user.password,
      C_REGDATE -> user.registerDate)

    if (withId)
      values ++ idColumns(user.userId)
    else
      values
  }

  def idColumns(id: Option[Long]): Seq[NamedParameter] =
    id.collect { case n => NamedParameter(C_ID, n) }.toSeq

  val userParser =
    (long(C_ID) ~ int(C_PHONE) ~ str(C_EMAIL) ~
      str(C_PASSWORD) ~ date(C_REGDATE)).map {
      case id ~ phone ~ email ~ pass ~ date =>
        User(Some(id), phone, email, pass, new DateTime(date))
    }

//  def save(user: User) = helper.insert(toData(user, withId = true))
  def update(user: User) =
    helper.update(toData(user, withId = false), idColumns(user.userId))
  def delete(id: Long) = helper.delete(idColumns(Some(id)))

  def find(id: Long): Either[User, String] = {
    DB.withConnection { implicit conn =>
      helper.runSql {
        anorm.SQL(
          s"""
            SELECT * FROM $TABLE_NAME WHERE $C_ID = $C_ID
          """
        ).on(idColumns(Some(id)): _*).as(userParser.singleOpt)
          .map(Left(_)).getOrElse(Right(s"Could not find user with id $id."))
      }
    }
  }

  def authenticate(phone: Int, password: String): Either[User, String] = {
    DB.withConnection { implicit conn =>
      helper.runSql {
        anorm.SQL(
          s"""
            SELECT * FROM $TABLE_NAME WHERE $C_PHONE = $C_PHONE AND $C_PASSWORD = $C_PASSWORD
          """
        ).on(C_PHONE -> phone, C_PASSWORD -> password).as(userParser.singleOpt)
          .map(Left(_)).getOrElse(Right(s"Could not login with the given username and password."))
      }
    }
  }
}