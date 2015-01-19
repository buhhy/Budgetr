package models

import org.joda.time.DateTime
import play.api.db.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath

sealed trait Role

case object NormalUser extends Role

case object Administrator extends Role

case class User(phone: String, email: String, password: String, role: Role = NormalUser)

case class InsertedUser(userId: Long, createDate: DateTime, user: User)

object User {
  private val JsonWriterBase = (JsPath \ "phone").write[String] and
      (JsPath \ "email").write[String] and
      (JsPath \ "password").write[String]

  val JsonReader = ((JsPath \ "phone").read[String] and
        (JsPath \ "email").read[String] and
        (JsPath \ "password").read[String]).apply { (phone, email, pass) =>
      User(phone, email, pass)
    }

  val NewJsonWriter = JsonWriterBase.apply { user: User => (user.phone, user.email, user.password)}

  val InsertedJsonWriter = ((JsPath \ "userId").write[Long] and
      (JsPath \ "createDate").write[DateTime]
      and NewJsonWriter).apply(unlift(InsertedUser.unapply))
}
