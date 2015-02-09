package planet7.extensions.implementation

import org.scalatest.{MustMatchers, WordSpec}
import planet7.extensions._

case class Transaction(accountId: String, desc: String, price: BigDecimal, lots: Int)

class PlusSpec extends WordSpec with MustMatchers {
  "Adding case classes sums Numerics, ORs Booleans and prefers the first nonEmpty String" in {
    val buy = Transaction("1", "buy", -26.3, 20)
    val sell = Transaction("1", "sell", 72.4, -30)

    buy |+| sell mustEqual Transaction("1", "buy", 46.1, -10)
  }

  "Worked example" in {
    val transactions: Seq[Transaction] = Seq(
      Transaction("1", "buy", -26.3,  10),
      Transaction("1", "sell", 72.4, -10),
      Transaction("2", "sell", 15.2, -10),
      Transaction("3", "buy", -29.3,  10),
      Transaction("2", "sell", 73.8, -10),
      Transaction("1", "buy", -93.2,  10))
    
    val expectedBalances = Seq(
      Transaction("1", "balance", -47.1,  10),
      Transaction("2", "balance",  89.0, -20),
      Transaction("3", "balance", -29.3,  10))

    val result = transactions
      .groupBy(_.accountId)
      .mapValues(st => st.reduceLeft((t1, t2) => t1 |+| t2))
      .values.toSeq
      .map(_.copy(desc = "balance"))
      .sortBy(_.accountId)

    result mustEqual expectedBalances
  }
}