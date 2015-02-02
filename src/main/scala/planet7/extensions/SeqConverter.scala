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
// TODO - CAS - 31/01/15 - to()
trait SeqConverter[T] {
  def from(s: Seq[String]): Try[T]
  def to(t: T): Seq[String] = ???
}

trait SeqConverterImplicits {
  import shapeless.examples.{CSVConverter, CSVException}

  def fail(s: String) = Failure(new CSVException(s))

  implicit def bigDecimalCsvConverter: CSVConverter[BigDecimal] = new CSVConverter[BigDecimal] {
    def from(s: String): Try[BigDecimal] = Try(BigDecimal(s))
    def to(i: BigDecimal): String = i.toString()
  }

  implicit def deriveHNil: SeqConverter[HNil] =
    new SeqConverter[HNil] {
      def from(s: Seq[String]): Try[HNil] = s match {
        case Nil => Success(HNil)
        case _ => fail("Cannot convert '" ++ s.toString ++ "' to HNil")
      }
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
    }

  // Generic.Aux[A,R] is equivalent to Generic.Aux[MyCaseClass,HListOfMyCaseClass]
  // To see the type of R: deriveClass[A,R: ClassTag] ... val rClazz = implicitly[ClassTag[R]].runtimeClass
  implicit def deriveCaseClassFromSeq[A, R](implicit gen: Generic.Aux[A, R], toHListConv: SeqConverter[R]): SeqConverter[A] =
    new SeqConverter[A] {
      def from(s: Seq[String]): Try[A] = toHListConv.from(s).map(gen.from)
    }
}
