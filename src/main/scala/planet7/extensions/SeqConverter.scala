/*
  Extends concepts defined in shapeless.examples.CSVConverter
  See https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/csv.scala
 */
package planet7.extensions

import shapeless._
import scala.util.{Failure, Success, Try}

object CaseClassConverter {
  def apply[T](implicit seqConv: Lazy[SeqConverter[T]]): SeqConverter[T] = seqConv.value
}

// TODO - CAS - 31/01/15 - GenTraversableLike, or whatever
trait SeqConverter[T] {
  def from(s: Seq[String]): Try[T]
  def to(t: T): Seq[String]
}

trait SeqConverterImplicits {
  import shapeless.examples.{CSVConverter, CSVException}

  def fail(s: String) = Failure(new CSVException(s))

  implicit def bigDecimalCsvConverter: CSVConverter[BigDecimal] = new CSVConverter[BigDecimal] {
    def from(s: String): Try[BigDecimal] = Try(BigDecimal(makeSafe(s))).recoverWith{
      case t: Throwable => Failure(StringConverterException(t, s))
    }

    private def makeSafe(s: String) = s.replaceAll(",", "").replaceAll("$^", "0.00")

    def to(i: BigDecimal): String = i.toString()
  }

  implicit def deriveHNilFromSeq: SeqConverter[HNil] =
    new SeqConverter[HNil] {
      def from(s: Seq[String]): Try[HNil] = s match {
        case Nil => Success(HNil)
        case _ => fail("Cannot convert '" ++ s.toString ++ "' to HNil")
      }

      def to(t: HNil): Seq[String] = Seq()
    }

  implicit def deriveHConsFromSeq[V, T <: HList](implicit stringConv: Lazy[CSVConverter[V]], seqConv: Lazy[SeqConverter[T]]): SeqConverter[V :: T] =
    new SeqConverter[V :: T] {
      def from(s: Seq[String]): Try[V :: T] = {
        (s.head, s.tail) match {
          case (head, tail) =>
            for {
              front <- stringConv.value.from(head)
              back <- seqConv.value.from(if (tail.isEmpty) Nil else tail)
            } yield front :: back

          case _ => fail("Cannot convert '" ++ s.mkString("[",",","]") ++ "' to HList")
        }
      }

      def to(t: V :: T): Seq[String] = stringConv.value.to(t.head) +: seqConv.value.to(t.tail)
    }

  // Generic.Aux[A,R] is equivalent to Generic.Aux[MyCaseClass,HListOfMyCaseClass]
  // To see the type of R: deriveClass[A,R: ClassTag] ... val rClazz = implicitly[ClassTag[R]].runtimeClass
  implicit def deriveCaseClassFromSeq[A, R](implicit gen: Generic.Aux[A, R], toHListConv: SeqConverter[R]): SeqConverter[A] =
    new SeqConverter[A] {
      def from(s: Seq[String]): Try[A] = toHListConv.from(s).map(gen.from)
      def to(t: A) = toHListConv.to(gen.to(t))
    }
}

case class StringConverterException(e: Throwable, originalValue: String) extends RuntimeException {
  override def toString = s"Could not convert value [$originalValue] to BigDecimal, because of: $e"
}
