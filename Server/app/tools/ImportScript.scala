package tools

import java.io.FileReader
import java.lang.Math

import com.github.tototoshi.csv.CSVReader
import models.Expense
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
  def run() {
    val reader = CSVReader.open(new FileReader("conf/data/sampledata.csv"))
    val iterator = reader.iterator

    var curDate: DateTime = DateTime.now

    do {
      iterator.next() match {
        case categoryOrDate :: business :: cost1 :: cost2 :: items =>
          categoryOrDate.split("/").toSeq match {
            case Seq(dd, mm, yy) =>
              // is a date
              curDate = new DateTime(
                  yy.toInt + 2000, mm.toInt, dd.toInt, 0, 0, DateTimeZone.forID("US/Pacific"))
            case _ =>
              // is a data row
              Expense(business, items.mkString(","), 0, 0, 0,
                  Math.round((cost1.toDouble + cost2.toDouble) * 100).toInt)
              println(s"$categoryOrDate $business $$$cost1 $$$cost2 $items")
          }
        case _ =>
          // invalid row, skip
          println("-----------------------------------------------")
      }
    } while (iterator.hasNext)
  }
}
