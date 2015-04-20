package tools

import java.io.FileReader

import com.github.tototoshi.csv.CSVReader
import controllers.common.ErrorType
import db._
import models._
import org.joda.time.{DateTimeZone, DateTime}
import play.api.Logger

/**
 * This Scala script is used to import the UWBC Excel spreadsheet. To runImport this script:
 *    - Run the Play console: activator
 *    - Open the Scala console within play: console
 *        new play.core.StaticApplication(new java.io.File("."))
 *        :paste -raw app/tools/BadmintonClubCSVImporter.scala
 *        ImportScript.runImport
 *
 * The Excel data must be in the following CSV format:
 *    dd/mm/yy
 *    [
 *      category, business, cost to person 1, cost to person 2, items
 *    ]
 *
 * @author tlei (Terence Lei)
 */
object ImportScript {
  private val USER_ID_JESSICA = 1
  private val USER_ID_TERENCE = 2

  private val EXP_LIST_ID_DEFAULT = 1

  private def toMoney(str: String): Int = {
    try {
      Math.round(str.toDouble * 100).toInt
    } catch {
      case e: Exception =>
        0
    }
  }

  // TODO(tlei): remove seed data
  private val DefaultUsers = Seq(
    (1, User("Jessica", "Fung", "6507729203", "jessicafung@live.ca", "dotovolvo123")),
    (2, User("Terence", "Lei", "6503907826", "terence.lei@live.ca", "dotovolvo456")))

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

  private def insertSeedData() {
    // TODO(tlei): remove seed data

    // Delete existing data.

    runTruncate(DBUserExpenseJoin.truncate())
    runTruncate(DBExpense.truncate)
    runTruncate(DBExpenseCategory.truncate)
    runTruncate(DBUserExpenseListJoin.truncate)
    runTruncate(DBExpenseList.truncate())
    runTruncate(DBUser.truncate)

    // Insert seed users.

    val added1 = DefaultUsers.map { u =>
      DBUser.insert(u._1, u._2) match {
        case Left(x) => 1
        case Right(error) =>
          Logger.error(error.message)
          0
      }
    }.sum

    Logger.info(s"Added $added1 default user entries.")

    // Insert seed expense lists.

    DBExpenseList.insert(DefaultExpenseList._1, DefaultExpenseList._2) match {
      case Left(x) =>
        Logger.info("Added expense list.")
        DefaultUsers.foreach { u =>
          val uej = DBUserExpenseListJoin.insert(UserExpenseListJoin(u._1, DefaultExpenseList._1)) match {
            case Left(_) => 1
            case Right(error) =>
              Logger.error(error.message)
              0
          }

          Logger.info(s"Added $uej users to expense list.")
        }
      case Right(error) =>
        Logger.error(error.message)
    }

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

  private def runImport() {
    val reader = CSVReader.open(new FileReader("conf/data/sampledata.csv"))
    val iterator = reader.iterator

    var curDate: DateTime = DateTime.now
    val categoryCache = scala.collection.mutable.HashMap[String, InsertedExpenseCategory]()

    do {
      iterator.next() match {
        case categoryOrDate :: business :: cost1 :: cost2 :: items =>
          categoryOrDate.split("/").toSeq match {
            case Seq(dd, mm, yy) =>
              // is a date
              curDate = new DateTime(
                  yy.toInt + 2000, mm.toInt, dd.toInt, 0, 0, DateTimeZone.forID("US/Pacific"))
              println(s"$curDate ----------------------------------------")

            case _ =>
              // is a data row
              val intCost1 = toMoney(cost1)
              val intCost2 = toMoney(cost2)
              val totalCost = intCost1 + intCost2
              val creatorId = if (intCost1 >= intCost2) USER_ID_TERENCE else USER_ID_JESSICA
              val catName = categoryOrDate.trim

              // identify if the category exists or not
              categoryCache.get(catName).orElse {
                DBExpenseCategory.insert(
                  ExpenseCategory(catName, creatorId, EXP_LIST_ID_DEFAULT)) match {
                  case Left(insertedCategory) =>
                    categoryCache.put(catName, insertedCategory)

                    Some(insertedCategory)
                  case Right(error) =>
                    println(error.message)
                    None
                }
              } match {
                case Some(category) =>
                  // create expense
                  DBExpense.insert(
                    Expense(
                      business, items.mkString(","), EXP_LIST_ID_DEFAULT,
                      creatorId, category.expCatId, totalCost), curDate) match {
                    case Left(insertedExpense) =>
                      println(
                        s"\t${insertedExpense.expense.location} - ${insertedExpense.expense.desc}: "
                            + insertedExpense.expense.amount)

                      // create user-expense connections
                      DBUserExpenseJoin.insertAll(
                        Seq(
                          UserExpenseJoin(
                            USER_ID_TERENCE, insertedExpense.expId,
                            if (totalCost == 0) 1.0 else intCost1.toDouble / totalCost,
                            0.5),
                          UserExpenseJoin(
                            USER_ID_JESSICA, insertedExpense.expId,
                            if (totalCost == 0) 0.0 else intCost2.toDouble / totalCost,
                            0.5))) match {
                        case Left(joinT :: joinJ :: Nil) =>
                          println(s"\t\tTerence: ${joinT.join.paidAmount}")
                          println(s"\t\tJessica: ${joinJ.join.paidAmount}")

                        case Right(error) =>
                          println(error.message)

                        case Left(list) =>
                          println(
                            s"Error: not all user-expense joins inserted, `${list.size}` found...")
                      }
                    case Right(error) =>
                      println(error.message)
                  }
                case None =>
                  println(s"Error: missing the category `$catName`")
              }
          }
        case _ =>
          // invalid row, skip
          println("-----------------------------------------------")
      }
    } while (iterator.hasNext)
  }

  def run() = {
    // Create Play application context for DB access
    new play.core.StaticApplication(new java.io.File("."))
    insertSeedData()
    runImport()
  }

  def main(args: Array[String]): Unit = run()
}
