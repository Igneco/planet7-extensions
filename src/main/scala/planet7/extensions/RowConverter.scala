package planet7.extensions

import planet7.tabular.Row
import shapeless.Generic

import scala.util.Try

trait RowConverter[T] {
  def fromRow(s: Row): Try[T]
  def toRow(t: T): Row
}

trait RowConverterImplicits {
  implicit def deriveCaseClassFromRow[A, R](implicit gen: Generic.Aux[A, R], toHListConv: SeqConverter[R]): RowConverter[A] =
    new RowConverter[A] {
      def fromRow(r: Row): Try[A] = toHListConv.from(r.data).map(gen.from)
      def toRow(t: A): Row = Row(toHListConv.to(gen.to(t)).toArray)
    }
}