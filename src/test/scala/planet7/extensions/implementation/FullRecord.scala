package planet7.extensions.implementation

case class FullRecord(id: String, firstName: String, surname: String, age: Int, address: String, fee: BigDecimal, surprise: String) {
  def firstOf(one: String, two: String): String = if (one.isEmpty) two else one

  // TODO - CAS - 03/02/15 - Generic implementation with HList and Poly2
  def +(that: FullRecord): FullRecord = new FullRecord(
    firstOf(id, that.id),
    firstOf(firstName, that.firstName),
    firstOf(surname, that.surname),
    Math.max(age, that.age),
    firstOf(address, that.address),
    fee + that.fee,
    firstOf(surprise, that.surprise)
  )
}

object FullRecordHelper {
  def combine(records: Iterable[FullRecord]): FullRecord = records.tail.foldLeft(records.head){ case (next, acc) => acc + next }

  val emptyRecord = FullRecord("", "", "", 0, "", BigDecimal("0.0"), "")
}