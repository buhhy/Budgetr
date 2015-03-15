package tools

import java.io.FileReader

import com.github.tototoshi.csv.CSVReader
import db.{DBExpenseCategory, DBUserExpenseJoin, DBExpense}
import models.{InsertedExpenseCategory, ExpenseCategory, UserExpenseJoin, Expense}
import org.joda.time.{DateTimeZone, DateTime}

/**
 * This Scala script is used to import the UWBC Excel spreadsheet. To run this script:
 *    - Run the Play console: activator
 *    - Open the Scala console within play: console
 *    - Execute this line: ':paste -raw app/tools/BadmintonClubCSVImporter.scala'
 *    - Run the script: 'ImportScript.run'
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

  def run() {
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
}
