package models

import org.joda.time.DateTime
import play.api.db.DB

case class User(
  userId: Option[Long], phone: Int,
  email: String, password: String, registerDate: DateTime)
