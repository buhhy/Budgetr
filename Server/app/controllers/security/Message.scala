package controllers.security

import jp.t2v.lab.play2.auth.AuthElement
import models.{Administrator, NormalUser}
import play.api.mvc.Controller

//object Message extends Controller with AuthElement with AuthenticationConfig {
//
//  // The `StackAction` method
//  //    takes `(AuthorityKey, Authority)` as the first argument and
//  //    a function signature `RequestWithAttributes[AnyContent] => Result` as the second argument and
//  //    returns an `Action`
//
//  // the `loggedIn` method
//  //     returns current logged in user
//
//  def main = StackAction(AuthorityKey -> NormalUser) { implicit request =>
//    val user = loggedIn
//    val title = "message main"
//    Ok(views.html.message.main(title))
//  }
//
//  def list = StackAction(AuthorityKey -> NormalUser) { implicit request =>
//    val user = loggedIn
//    val title = "all messages"
//    Ok(views.html.message.list(title))
//  }
//
//  def detail(id: Int) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
//    val user = loggedIn
//    val title = "messages detail "
//    Ok(views.html.message.detail(title + id))
//  }
//
//  // Only Administrator can execute this action.
//  def write = StackAction(AuthorityKey -> Administrator) { implicit request =>
//    val user = loggedIn
//    val title = "write message"
//    Ok(views.html.message.write(title))
//  }
//}