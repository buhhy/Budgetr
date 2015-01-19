package models

import org.joda.time.DateTime
import play.api.db.DB

sealed trait Role
case object NormalUser extends Role
case object Administrator extends Role

case class User(
  userId: Option[Long], phone: String, email: String, password: String,
  registerDate: Option[DateTime], role: Role = NormalUser)
