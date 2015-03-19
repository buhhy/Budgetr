import controllers.common.ErrorType
import db._
import models.{ExpenseCategory, UserExpenseListJoin, ExpenseList, User}
import org.joda.time.DateTimeZone
import play.api.{Application, GlobalSettings, Logger}

object Global extends GlobalSettings {
  // TODO(tlei): remove seed data
  private val DefaultUsers = Seq(
    (1, User("6507729203", "jessicafung@live.ca", "dotovolvo123")),
    (2, User("6503907826", "terence.lei@live.ca", "dotovolvo456")))

  private val DefaultExpenseList = (1, ExpenseList(1, "Test list", "this is some good stuff"))

  private val DefaultExpenseCategories = Seq(
    (1, ExpenseCategory("Food", 1, 1)),
    (2, ExpenseCategory("Transportation", 1, 1)),
    (3, ExpenseCategory("Housing", 2, 1)))

  private def runTruncate(f: => Either[Int, ErrorType]) = {
    f match {
      case Left(result) =>
        Logger.info(s"$result items dropped")
      case Right(error) =>
        Logger.error(error.message)
    }
  }

  override def onStart(app: Application): Unit = {
    Logger.info("Setting global time zone to UTC...")
    DateTimeZone.setDefault(DateTimeZone.UTC)

    // TODO(tlei): remove seed data

    // Delete existing data.

//    runTruncate(DBUserExpenseJoin.truncate)
//    runTruncate(DBExpense.truncate)
//    runTruncate(DBExpenseCategory.truncate)
//    runTruncate(DBUserExpenseListJoin.truncate)
//    runTruncate(DBExpenseList.truncate)
//    runTruncate(DBUser.truncate)
//
//
//
//
//    // Insert seed users.
//
//    val added1 = DefaultUsers.map { u =>
//      DBUser.insert(u._1, u._2) match {
//        case Left(x) => 1
//        case Right(error) =>
//          Logger.error(error.message)
//          0
//      }
//    }.sum
//
//    Logger.info(s"Added $added1 default user entries.")
//
//    // Insert seed expense lists.
//
//    DBExpenseList.insert(DefaultExpenseList._1, DefaultExpenseList._2) match {
//      case Left(x) =>
//        Logger.info("Added expense list.")
//        DefaultUsers.foreach { u =>
//          val uej = DBUserExpenseListJoin.insert(UserExpenseListJoin(u._1, DefaultExpenseList._1)) match {
//            case Left(_) => 1
//            case Right(error) =>
//              Logger.error(error.message)
//              0
//          }
//
//          Logger.info(s"Added $uej users to expense list.")
//        }
//      case Right(error) =>
//        Logger.error(error.message)
//    }

    // Insert seed expense categories.

//    val added2 = DefaultExpenseCategories.map { c =>
//      DBExpenseCategory.insert(c._1, c._2) match {
//        case Left(x) => 1
//        case Right(error) =>
//          Logger.error(error.message)
//          0
//      }
//    }.sum
//
//    Logger.info(s"Added $added2 default expense category entries.")
  }
}
