package shape.ful

import org.scalatest.{MustMatchers, WordSpec}

import planet7.tabular._

import shapeless._
import shapeless.examples.CSVConverter

import scala.util.Try

case class ActualPerson(id: Int, firstName: String, surname: String, fee: BigDecimal)

// TODO - CAS - 30/01/15 - Use Array data elements, instead of concatenating them back into a String
// TODO - CAS - 30/01/15 - Work out the best way to present errors
// TODO - CAS - 30/01/15 - Supply better error messages in the toNumberType-style implicit conversions
class ShapePlay extends WordSpec with MustMatchers {

  implicit def bigDecimalCsvConverter: CSVConverter[BigDecimal] = new CSVConverter[BigDecimal] {
    def from(s: String): Try[BigDecimal] = Try(BigDecimal(s))
    def to(i: BigDecimal): String = i.toString()
  }

  "We can convert Csv rows to much more betterer case classes" in {
    val input = """ID,First Name,Surname,Fee
                  |1,Sue,Smith,10.0
                  |3,Bob,Smith,12.2
                  |4,Fred,Black,11.1
                  |5,Jeremiah,Jones,13.3""".stripMargin

    val csv = Csv(input)

    val maybeResources: Iterator[Try[ActualPerson]] = csv.iterator.map(row => CSVConverter[ActualPerson].from(row.data.mkString(",")))

    println(s"maybeResources.mkString(): ${maybeResources.mkString("\n")}")
  }

  "All the failure cases" in {
    val input = """ID,First Name,Surname,Fee
                  |dog,Sue,Smith,10.0
                  |3,Bob,Smith,rabbit
                  |,Fred,Black,11.1
                  |5,Jeremiah,Jones,13.3""".stripMargin

    val csv = Csv(input)

    val maybeResources: Iterator[Try[ActualPerson]] = csv.iterator.map(row => CSVConverter[ActualPerson].from(row.data.mkString(",")))

    val partitioned: (Iterator[Try[ActualPerson]], Iterator[Try[ActualPerson]]) = maybeResources.partition(_.isSuccess)

    println(s"success:\n${partitioned._1.toList.mkString("\n")}")
    println(s"failure:\n${partitioned._2.toList.mkString("\n")}")
  }

  "Simples" in {
    val input = """ID,First Name,Surname,Fee
                  |5,Jeremiah,Jones,13.3""".stripMargin

    val csv = Csv(input)

    val maybeResources: Iterator[Try[ActualPerson]] = csv.iterator.map(row => CSVConverter[ActualPerson].from(row.data.mkString(",")))

    val partitioned: (Iterator[Try[ActualPerson]], Iterator[Try[ActualPerson]]) = maybeResources.partition(_.isSuccess)

    println(s"success:\n${partitioned._1.toList.mkString("\n")}")
    println(s"failure:\n${partitioned._2.toList.mkString("\n")}")
  }
}
