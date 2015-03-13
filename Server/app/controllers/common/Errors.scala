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

case class MultiError(errors: Seq[ErrorType]) extends ErrorType {
  override protected def msg: String = s"$errorType: [ ${errors.map(_.message).mkString("\n\t")} ]"
}

package object Errors {
  type ResultWithError[A] = Either[A, ErrorType]
  val NoJsonError = JSONError("No JSON object was provided.")
  val NoJsonValueFieldError = JSONError("JSON object does not contain the `value` field.")
}
