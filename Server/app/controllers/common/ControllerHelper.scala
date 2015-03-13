package controllers.common

import play.api.mvc.{Results, AnyContent, Request, Result}
import play.api.libs.json.{Json, JsValue}

/**
 * Collection of commonly used controller related utility functions.
 * @author tlei (Terence Lei)
 */
object ControllerHelper {
  def withJsonRequest(f: JsValue => Result)(implicit req: Request[AnyContent]): Result = {
    req.body.asJson match {
      case Some(json) =>
        (json \ "value").asOpt[JsValue] match {
          case Some(value) =>
            f(value)
          case None =>
            Results.BadRequest(Errors.NoJsonValueFieldError.toJson)
        }
      case None =>
        Results.BadRequest(Errors.NoJsonError.toJson)
    }
  }
}
