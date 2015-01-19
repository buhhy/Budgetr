package controllers

import _root_.db.DBUser
import controllers.security.AuthenticationConfig
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.{NormalUser, User}
import play.api.data.{Forms, Form}
import play.api.mvc._
import play.api.data.Forms._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Application extends Controller with LoginLogout with AuthElement with AuthenticationConfig {
  def index = Action {
    Ok(views.html.index(Nil))
  }

  def dashboard = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    Ok(views.html.dashboard(loggedIn))
  }

  /**
   * Return the `gotoLogoutSucceeded` method's result in the logout action.
   *
   * Since the `gotoLogoutSucceeded` returns `Future[Result]`,
   * you can add a procedure like the following.
   *
   *   gotoLogoutSucceeded.map(_.flashing(
   *     "success" -> "You've been logged out"
   *   ))
   */
  def logout = Action.async { implicit request =>
    // do something...
    gotoLogoutSucceeded
  }

  /**
   * Return the `gotoLoginSucceeded` method's result in the login action.
   *
   * Since the `gotoLoginSucceeded` returns `Future[Result]`,
   * you can add a procedure like the `gotoLogoutSucceeded`.
   */
  def authenticate = Action.async { implicit request =>
    val form = Form(tuple("phone" -> text, "password" -> text))
    form.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.index(form.errors.map(_.message)))),
      { case (phone, pass) =>
        DBUser.authenticate(phone, pass) match {
          case Left(User(Some(id), _, _, _, _, _)) =>
            gotoLoginSucceeded(id)
          case Right(msg) =>
            Future.successful(Forbidden(views.html.index(Seq(msg.message))))
          case _ =>
            Future.successful(BadRequest(views.html.index(Seq("Something weird happened..."))))
        }
      }
    )
  }
}