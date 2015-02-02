package planet7.extensions

import planet7.tabular.Row
import shapeless.{Lazy, Generic}

import scala.util.Try

trait RowConverter[T] {
  def fromRow(s: Row): Try[T]
  def to(t: T): Seq[String] = ???
}

trait RowConverterImplicits {
  implicit def deriveCaseClassFromRow[A, R](implicit gen: Generic.Aux[A, R], toHListConv: SeqConverter[R]): RowConverter[A] =
    new RowConverter[A] {
      def fromRow(r: Row): Try[A] = toHListConv.from(r.data).map(gen.from)
    }
}