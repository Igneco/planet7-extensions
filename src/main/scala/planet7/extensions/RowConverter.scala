package planet7.extensions

import planet7.tabular.Row
import shapeless.Generic

import scala.util.{Success, Failure, Try}

trait RowConverter[T] {
  def fromRow(s: Row): Try[T]
  def toRow(t: T): Row
}

trait RowConverterImplicits {
  implicit def deriveCaseClassFromRow[A, R](implicit gen: Generic.Aux[A, R], toHListConv: SeqConverter[R]): RowConverter[A] =
    new RowConverter[A] {
      def fromRow(r: Row): Try[A] =
        toHListConv.from(r.data) match {
          case Success(hlist) => Success(gen.from(hlist))
          case Failure(e) => Failure(RowConverterException(e, r))
        }

      def toRow(t: A): Row = Row(toHListConv.to(gen.to(t)).toArray)
    }
}

case class RowConverterException(wrapped: Throwable, row: Row) extends RuntimeException {
  override def toString =
    s"""Failed to convert row:
       |  Row: $row
       |  Error: $wrapped
       |""".stripMargin
}