import controllers.common.ErrorType
import db._
import models.{ExpenseCategory, UserExpenseListJoin, ExpenseList, User}
import org.joda.time.DateTimeZone
import play.api.{Application, GlobalSettings, Logger}
import tools.ImportScript

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    Logger.info("Setting global time zone to UTC...")
    DateTimeZone.setDefault(DateTimeZone.UTC)
  }
}
