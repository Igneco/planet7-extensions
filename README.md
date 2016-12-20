Planet7 Extensions
==================

Planet7-extensions adds a couple of handy features to Planet7:
* Convert from `Csv` `Row`s to case classes and back again
* Add case classes together (for summing up transactions, rectangular co-ordinates, etc)

The code is based on the excellent [examples provided with Shapeless](https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/)

CSV Row <=> Case Class
----------------------
Given:
```scala
case class ActualPerson(id: Int, firstName: String, surname: String, fee: BigDecimal)
```

Converting a `Row` to a case class returns a `Try`, since there is a runtime element in the data conversion:
```scala
import planet7.extensions._

"ConvertTo case class from Row" in {
  val row: Row = Row(Array("5", "Jeremiah", "Jones", "13.3"))

  val triedPerson: Try[ActualPerson] = ConvertTo[ActualPerson].fromRow(row)

  triedPerson.get mustEqual ActualPerson(5, "Jeremiah", "Jones", 13.3)
}
```

We can always convert a case class to a `Row`, so no `Try` is needed here:
```scala
"ConvertTo to Row from case class" in {
  val person = ActualPerson(5, "Jeremiah", "Jones", 13.3)

  val row = ConvertFrom[ActualPerson].toRow(person)

  row mustEqual Row(Array("5", "Jeremiah", "Jones", "13.3"))
}
```

Case Class + Case Class
-----------------------

```scala
case class Transaction(accountId: String, desc: String, price: BigDecimal, lots: Int)

"Adding case classes sums Numerics, ORs Booleans and prefers the first nonEmpty String" in {
  val buy = Transaction("1", "buy", -26.3, 20)
  val sell = Transaction("1", "sell", 72.4, -30)

  buy |+| sell mustEqual Transaction("1", "buy", 46.1, -10)
}
```
