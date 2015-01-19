package models

import org.joda.time.DateTime
import play.api.db.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath

sealed trait Role
case object NormalUser extends Role
case object Administrator extends Role

case class User(
  userId: Option[Long], phone: String, email: String, password: String,
  createDate: Option[DateTime], role: Role = NormalUser)

object User {
  val JsonReader =
    ((JsPath \ "userId").readNullable[Long] and
        (JsPath \ "phone").read[String] and
        (JsPath \ "email").read[String] and
        (JsPath \ "password").read[String]).apply { (id, phone, email, pass) =>
      User(id, phone, email, pass, None)
    }

  val JsonWriter = ((JsPath \ "userId").writeNullable[Long] and
        (JsPath \ "phone").write[String] and
        (JsPath \ "email").write[String] and
        (JsPath \ "password").write[String] and
        (JsPath \ "createDate").writeNullable[DateTime]).apply { user: User =>
    (user.userId, user.phone, user.email, user.password, user.createDate)
  }
}
