package planet7.extensions.implementation

import org.scalatest.{MustMatchers, WordSpec}
import planet7.extensions._
import planet7.tabular._

import scala.util.Try

class SeqConverterSpec extends WordSpec with MustMatchers {
  "Single class example" in {
    val seq: Seq[String] = Seq("5", "Jeremiah", "Jones", "13.3")

    val triedPerson = CaseClassConverter[ActualPerson].from(seq).get

    triedPerson mustEqual ActualPerson(5, "Jeremiah", "Jones", 13.3)
  }

  "Row in Csv example" in {
    val input = """ID,First Name,Surname,Fee
                  |5,Jeremiah,Jones,13.3""".stripMargin

    val maybeResources: Iterator[Try[ActualPerson]] =
      Csv(input).iterator.map(row => CaseClassConverter[ActualPerson].from(row.data))

    val partitioned: (Iterator[Try[ActualPerson]], Iterator[Try[ActualPerson]]) = maybeResources.partition(_.isSuccess)

    println(s"success:\n${partitioned._1.toList.mkString("\n")}")
    println(s"failure:\n${partitioned._2.toList.mkString("\n")}")
    fail("Be more assertive")
  }
}