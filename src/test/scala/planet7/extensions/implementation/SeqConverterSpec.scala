package planet7.extensions.implementation

import org.scalatest.{MustMatchers, WordSpec}
import planet7.Diff
import planet7.extensions._
import planet7.tabular._

import scala.util.{Failure, Success, Try}

// TODO - CAS - 01/02/15 - Publish and use
// TODO - CAS - 01/02/15 - Add assertions to specs
// TODO - CAS - 30/01/15 - Work out the best way to present errors
// TODO - CAS - 30/01/15 - Supply better error messages in the toNumberType-style implicit conversions
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

  "We can merge Csvs on a key column" in {
    val name = Csv( """ID,First Name,Surname,Fee
                      |1,Sue,Smith,10
                      |3,Bob,Smith,12
                      |4,Fred,Black,11
                      |5,Jeremiah,Jones,13""".stripMargin)

    val ageAndAddress = Csv( """ID,Age,Address,Fee
                               |1,24,18 Monkey Street,6
                               |2,36,94 Elephant Street,7
                               |4,127,6 Otter Passage,8
                               |5,36,47 Baboon Way,9""".stripMargin)

    val expected = Csv( """ID,First Name,Surname,Age,Address,Fee,Surprise
                          |1,Sue,Smith,24,18 Monkey Street,16,
                          |2,,,36,94 Elephant Street,7,
                          |3,Bob,Smith,0,,12,
                          |4,Fred,Black,127,6 Otter Passage,19,
                          |5,Jeremiah,Jones,36,47 Baboon Way,22,""".stripMargin)

    val outputColumns = Seq("ID", "First Name", "Surname", "Age", "Address", "Fee", "Surprise")
    val outputStructure: Seq[(String, String)] = outputColumns

    def defaultTo(other: String) = (s: String) => if(s.isEmpty) other else s

    val condensed =
      Csv(
        name.columnStructure(outputStructure: _*),
        ageAndAddress.columnStructure(outputStructure: _*)
      )
      .withMappings(
        "Age" -> defaultTo("0"),
        "Fee" -> defaultTo("0.0")
      )
      .map(ConvertTo[FullRecord].fromRow)
      .map {
        case Success(fr) => fr
        case Failure(e) => FullRecordHelper.emptyRecord
      }
      .groupBy(_.id)
      .mapValues(FullRecordHelper.combine)
      .valuesIterator
      .map(_.toRow)

    val result: Csv = Csv(Row(outputColumns.toArray), condensed)

    Diff(result, expected, NaiveRowDiffer) mustBe empty
  }
}

case class FullRecord(id: String, firstName: String, surname: String, age: Int, address: String, fee: BigDecimal, surprise: String) {
  def firstOf(one: String, two: String): String = if (one.isEmpty) two else one
  def +(that: FullRecord): FullRecord = new FullRecord(
    firstOf(id, that.id),
    firstOf(firstName, that.firstName),
    firstOf(surname, that.surname),
    Math.max(age, that.age),
    firstOf(address, that.address),
    fee + that.fee,
    firstOf(surprise, that.surprise)
  )

  def toRow = Row(Array(id, firstName, surname, age.toString, address, fee.toString(), surprise))
}

object FullRecordHelper {
  def combine(records: Iterable[FullRecord]): FullRecord = records.tail.foldLeft(records.head){ case (next, acc) => acc + next }

  val emptyRecord = FullRecord("", "", "", 0, "", BigDecimal("0.0"), "")
}