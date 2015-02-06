package planet7.extensions

import shapeless.examples.Monoid
import shapeless.examples.MonoidSyntax

/**
 * Example from shapeless.examples.monoids.csv modified here to support BigDecimal and not
 * concatenate Strings
 */
trait MonoidImplicits {

  implicit def bigDecimalMonoid: Monoid[BigDecimal] = new Monoid[BigDecimal] {
    def zero = 0.0
    def append(a: BigDecimal, b: BigDecimal) = a + b
  }

  implicit def stringMonoid: Monoid[String] = new Monoid[String] {
    def zero = ""
    def append(a: String, b: String) = if (a.isEmpty) b else a
  }

  implicit def monoidSyntax[T](a: T)(implicit mt: Monoid[T]): MonoidSyntax[T] = new MonoidSyntax[T] {
    def |+|(b: T) = mt.append(a, b)
  }
}