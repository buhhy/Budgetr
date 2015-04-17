package models

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath

sealed trait Role

case object NormalUser extends Role

case object Administrator extends Role

case class User(
    firstName: String, lastName: String, phone: String, email: String, password: String,
    role: Role = NormalUser)

case class InsertedUser(userId: Long, createDate: DateTime, user: User)

object User {
  val JsonReader = ((JsPath \ "firstName").read[String]
      and (JsPath \ "lastName").read[String]
      and (JsPath \ "phone").read[String]
      and (JsPath \ "email").read[String]
      and (JsPath \ "password").read[String]).apply { (fname, lname, phone, email, pass) =>
    User(fname, lname, phone, email, pass)
  }
  
  val JsonWriter = ((JsPath \ "firstName").write[String]
      and (JsPath \ "lastName").write[String]
      and (JsPath \ "phone").write[String]
      and (JsPath \ "email").write[String]).apply { user: User =>
    (user.firstName, user.lastName, user.phone, user.email)
  }
}
  
object InsertedUser {
  val JsonWriter = ((JsPath \ "userId").write[Long]
      and (JsPath \ "createDate").write[DateTime]
      and User.JsonWriter).apply(unlift(InsertedUser.unapply))
}
