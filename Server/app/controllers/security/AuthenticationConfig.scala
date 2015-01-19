package controllers.security

import controllers.routes
import db.DBUser
import jp.t2v.lab.play2.auth.AuthConfig
import models.{Administrator, NormalUser}
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait AuthenticationConfig extends AuthConfig {
  /**
   * Refers to the session key that identifies the original page a non-logged-in user attempts to
   * reach. Retrieving the URI using this key allows us to redirect the user back to the original
   * URI following successful log in.
   */
  val ACCESS_URI_KEY = "access_uri"

  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = Long
  type User = models.InsertedUser

  /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait:
   *
   * sealed trait Role
   * case object Administrator extends Role
   * case object NormalUser extends Role
   */
  type Authority = models.Role

  /**
   * A `ClassTag` is used to retrieve an id from the Cache API.
   * Use something like this:
   */
  val idTag: ClassTag[Id] = scala.reflect.classTag[Id]

  /**
   * The session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600

  /**
   * A function that returns a `User` object from an `Id`.
   * You can alter the procedure to suit your application.
   */
  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] =
    Future(DBUser.find(id).fold(Some(_), _ => None))

  /**
   * Where to redirect the user after a successful login.
   */
  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = {
    val uri = request.session.get(ACCESS_URI_KEY)
        .getOrElse(routes.Application.dashboard().url.toString)
    Future.successful(Results.Redirect(uri).withSession(request.session - "access_uri"))
  }

  /**
   * Where to redirect the user after logging out
   */
  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Results.Redirect(routes.Application.index()))

  /**
   * If the user is not logged in and tries to access a protected resource then redirect them
   * as follows:
   */
  def authenticationFailed(request: RequestHeader)
      (implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(
      Results.Redirect(routes.Application.index()).withSession(ACCESS_URI_KEY -> request.uri))

  /**
   * If authorization failed (usually incorrect password) redirect the user as follows:
   */
  def authorizationFailed(request: RequestHeader)
      (implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(
      Results.Redirect(routes.Application.index()).withSession(ACCESS_URI_KEY -> request.uri))

  /**
   * A function that determines what `Authority` a user has.
   * You should alter this procedure to suit your application.
   */
  def authorize(user: User, authority: Authority)
      (implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    (user.user.role, authority) match {
      case (Administrator, _) => true
      case (NormalUser, NormalUser) => true
      case _ => false
    }
  }

  /**
   * Whether use the secure option or not use it in the cookie.
   * However default is false, I strongly recommend using true in a production.
   */
  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)

  /**
   * Whether a login session is closed when the browser is terminated.
   * default is false.
   */
  override lazy val isTransientCookie: Boolean = false

}