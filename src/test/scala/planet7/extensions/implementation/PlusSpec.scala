package planet7.extensions.implementation

import org.scalatest.{MustMatchers, WordSpec}
import planet7.extensions._

case class Transaction(account: Int, desc: String, amount: BigDecimal)

class PlusSpec extends WordSpec with MustMatchers {
  "I can add case classes" in {
    val transactions: Seq[Transaction] = Seq(
      Transaction(1, "buy X", -26.3),
      Transaction(1, "sell Y", 72.4),
      Transaction(2, "sell Z", 15.2),
      Transaction(3, "buy A", -29.3),
      Transaction(2, "sell B", 73.8),
      Transaction(1, "buy C", -93.2))

    val result = transactions.reduceLeft((t1, t2) => t1 |+| t2)

    result mustEqual Transaction(10,"buy X",12.6)
  }
}