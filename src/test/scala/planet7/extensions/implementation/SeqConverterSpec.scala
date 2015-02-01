package planet7.extensions.implementation

import org.scalatest.{MustMatchers, WordSpec}
import planet7.extensions.{ActualPerson, SeqConverter}
import planet7.tabular._

import scala.util.Try

// TODO - CAS - 30/01/15 - Work out the best way to present errors
// TODO - CAS - 30/01/15 - Supply better error messages in the toNumberType-style implicit conversions
class SeqConverterSpec extends WordSpec with MustMatchers {
  import planet7.extensions.SeqConverter._

  "Single class example" in {
    val seq: Seq[String] = Seq("5", "Jeremiah", "Jones", "13.3")

    val triedPerson: Try[ActualPerson] = SeqConverter[ActualPerson].from(seq)

    println(s"triedPerson: ${triedPerson}")
  }

  "Row in Csv example" in {
    val input = """ID,First Name,Surname,Fee
                  |5,Jeremiah,Jones,13.3""".stripMargin

    val maybeResources: Iterator[Try[ActualPerson]] =
      Csv(input).iterator.map(row => SeqConverter[ActualPerson].from(row.data))

    val partitioned: (Iterator[Try[ActualPerson]], Iterator[Try[ActualPerson]]) = maybeResources.partition(_.isSuccess)

    println(s"success:\n${partitioned._1.toList.mkString("\n")}")
    println(s"failure:\n${partitioned._2.toList.mkString("\n")}")
  }
}