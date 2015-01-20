import db.DBUser
import models.User
import org.joda.time.DateTimeZone
import play.api.{Application, GlobalSettings, Logger}

object Global extends GlobalSettings {
  // TODO(tlei): remove seed data
  private val DefaultUsers = Seq(
    (1, User("6507729203", "jessicafung@live.ca", "password2")),
    (2, User("6503907826", "terence.lei@live.ca", "password3")))

  override def onStart(app: Application): Unit = {
    Logger.info("Setting global time zone to UTC...")
    DateTimeZone.setDefault(DateTimeZone.UTC)

    // TODO(tlei): remove seed data
    val deleted = DefaultUsers.map { u =>
      DBUser.delete(u._1) match {
        case Left(x) => x
        case Right(error) =>
          Logger.error(error.message)
          0
      }
    }.sum

    Logger.info(s"Deleted $deleted default user entries.")

    val added = DefaultUsers.map { u =>
      DBUser.insert(u._1, u._2) match {
        case Left(x) => 1
        case Right(error) =>
          Logger.error(error.message)
          0
      }
    }.sum

    Logger.info(s"Added $added default user entries.")
  }
}
