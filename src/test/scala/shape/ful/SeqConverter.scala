package shape.ful

/*
 * Copyright (c) 2014 Mario Pastorelli (pastorelli.mario@gmail.com)
 *
 * Modified 2015 by Chris Agmen-Smith, to allow handling of Csv data that 
 * has already been parsed into rows of data elements
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import planet7.tabular.Row
import shapeless._, syntax.singleton._
import shapeless.examples.CSVConverter

import scala.util.{Try,Success,Failure}

class CSVException(s: String) extends RuntimeException

// TODO - CAS - 31/01/15 - GenTraversableLike, or whatever
// TODO - CAS - 31/01/15 - to()
trait SeqConverter[T] {
  def from(s: Seq[String]): Try[T]
  def to(t: T): Seq[String] = ???
}

object SeqConverter {
  def apply[T](implicit st: Lazy[SeqConverter[T]]): SeqConverter[T] = st.value

  def fail(s: String) = Failure(new CSVException(s))

  // HList
  implicit def deriveHNil: SeqConverter[HNil] =
    new SeqConverter[HNil] {
      def from(s: Seq[String]): Try[HNil] = s match {
        case Nil => Success(HNil)
        case _ => fail("Cannot convert '" ++ s.toString ++ "' to HNil")
      }
    }

  // HList
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

  // Any case class. Generic.Aux[A,R] is equivalent to Generic.Aux[MyCaseClass,HListOfMyCaseClass]
  // To see the type of R: deriveClass[A,R: ClassTag] ... val rClazz = implicitly[ClassTag[R]].runtimeClass
  implicit def deriveClassFromSeq[A, R](implicit gen: Generic.Aux[A, R], toHListConv: SeqConverter[R]): SeqConverter[A] =
    new SeqConverter[A] {
      def from(s: Seq[String]): Try[A] = toHListConv.from(s).map(gen.from)
    }
}
