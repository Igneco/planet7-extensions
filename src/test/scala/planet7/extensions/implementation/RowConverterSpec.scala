package planet7.extensions.implementation

import org.scalatest.{MustMatchers, WordSpec}
import planet7.Diff
import planet7.extensions._
import planet7.tabular._

import scala.util.{Failure, Success}

// TODO - CAS - 01/02/15 - Add assertions to specs
// TODO - CAS - 30/01/15 - Work out the best way to present errors
// TODO - CAS - 30/01/15 - Supply better error messages in the toNumberType-style implicit conversions
class RowConverterSpec extends WordSpec with MustMatchers {
  "ConvertTo case class from Row" in {
    val row: Row = Row(Array("5", "Jeremiah", "Jones", "13.3"))

    val triedPerson = ConvertTo[ActualPerson].fromRow(row).get

    triedPerson mustEqual ActualPerson(5, "Jeremiah", "Jones", 13.3)
  }

  "ConvertTo to Row from case class" in {
    val person = ActualPerson(5, "Jeremiah", "Jones", 13.3)

    val row = ConvertFrom[ActualPerson].toRow(person)

    row mustEqual Row(Array("5", "Jeremiah", "Jones", "13.3"))
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

    val condensed =
      Csv(
        name.columnStructure(outputStructure: _*),
        ageAndAddress.columnStructure(outputStructure: _*))
      .withMappings(
        "Age" -> defaultTo("0"),
        "Fee" -> defaultTo("0.0"))
      .map(ConvertTo[FullRecord].fromRow)
      .map {
        case Success(fr) => fr
        case Failure(e) => FullRecordHelper.emptyRecord
      }
      .groupBy(_.id)
      .mapValues(FullRecordHelper.combine)
      .valuesIterator
      .map(ConvertFrom[FullRecord].toRow)

    val result: Csv = Csv(Row(outputColumns.toArray), condensed)

    Diff(result, expected, NaiveRowDiffer) mustBe empty
  }
}

