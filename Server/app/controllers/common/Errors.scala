package controllers.common

import play.api.libs.json.{JsObject, Json}

trait ErrorType {
  protected def msg: String
  protected def errorType: String = getClass.getSimpleName

  def message: String = s"$errorType: $msg"
  def toJson: JsObject = Json.obj("errorType" -> errorType, "message" -> msg)
}

case class DBError(msg: String) extends ErrorType

case class JSONError(msg: String) extends ErrorType

case class AuthenticationError(msg: String) extends ErrorType
