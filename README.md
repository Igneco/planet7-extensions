Planet7 Extensions
==================

Based on the excellent examples provided with Shapeless, planet7-extensions adds a couple of handy features to Planet7.

https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/

CSV Row <=> Case Class
----------------------

```scala
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
```

Case Class + Case Class
-----------------------

```scala
"Adding case classes sums Numerics, ORs Booleans and prefers the first nonEmpty String" in {
  val buy = Transaction("1", "buy", -26.3, 20)
  val sell = Transaction("1", "sell", 72.4, -30)

  buy |+| sell mustEqual Transaction("1", "buy", 46.1, -10)
}
```
