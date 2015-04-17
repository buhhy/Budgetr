package db

import anorm.SqlParser._
import anorm.{NamedParameter, _}
import controllers.common.{AuthenticationError, DBError, ErrorType}
import controllers.security.PasswordHasher
import models.{InsertedUser, User}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.DB
import db.common.{AnormInsertHelper, AnormHelper}

object DBUser {
  private[db] val TableName = "user"
  private[db] val C_ID = "user_id"
  private[db] val C_FirstName = "first_name"
  private[db] val C_LastName = "last_name"
  private[db] val C_Phone = "phone"
  private[db] val C_Email = "email"
  private[db] val C_Password = "password"
  private[db] val C_CreateDate = "create_date"

  private val helper = new AnormHelper(TableName)
  private val insertHelper = AnormInsertHelper(TableName, C_CreateDate)

  val UserParser =
    (long(C_ID) ~ str(C_FirstName) ~ str(C_LastName) ~ str(C_Phone)~ str(C_Email)
        ~ str(C_Password) ~ date(C_CreateDate)).map {
      case id ~ fname ~ lname ~ phone ~ email ~ pass ~ date =>
        InsertedUser(id, new DateTime(date), User(fname, lname, phone, email, pass))
    }


  def toData(user: User): Seq[NamedParameter] = {
    Seq(
      C_FirstName -> user.firstName,
      C_LastName -> user.lastName,
      C_Phone -> user.phone,
      C_Email -> user.email,
      C_Password -> user.password)
  }

  def toData(user: InsertedUser): Seq[NamedParameter] = {
    toData(user.user) ++ Seq[NamedParameter](
      idColumn(user.userId),
      C_CreateDate -> user.createDate)
  }

  def idColumn(id: Long): NamedParameter = NamedParameter(C_ID, id)

  def insert(user: User): Either[InsertedUser, ErrorType] = insert(None, user)

  def insert(id: Long, user: User): Either[InsertedUser, ErrorType] = insert(Some(id), user)

  private def insert(id: Option[Long], user: User): Either[InsertedUser, ErrorType] = {
    // Hash the password
    val hashedPassword = user.copy(password = PasswordHasher.hashPassword(user.password))
    insertHelper.insert(toData(hashedPassword) ++ id.map(idColumn), None).fold(
      id => Left(InsertedUser(id._1, id._2, hashedPassword)),
      err => Right(err))
  }

  def update(id: Long, user: User) = helper.update(toData(user), Seq(idColumn(id)))

  def delete(id: Long) = helper.delete(Seq(idColumn(id)))
  def truncate = helper.truncate()

  def find(id: Long): Either[Option[InsertedUser], ErrorType] = {
    DB.withConnection { implicit conn =>
      AnormHelper.runSql {
        anorm.SQL(
          s"""
              |SELECT * FROM $TableName WHERE $C_ID = ${AnormHelper.replaceStr(C_ID)}
           """.stripMargin)
            .on(idColumn(id))
            .as(UserParser.singleOpt)
            .map(x => Left(Some(x)))
            .getOrElse(Left(None))
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
          """.stripMargin
        ).on(C_Phone -> phone).as(UserParser.singleOpt).map { u =>
          if (PasswordHasher.checkPassword(password, u.user.password))
            Some(Left(u))
          else
            None
        }.flatten.getOrElse(
              Right(AuthenticationError("Could not login with the given username and password.")))
      }
    }
  }
}