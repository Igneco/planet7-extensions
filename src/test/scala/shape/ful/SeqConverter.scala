package shape.ful

/*
 * Copyright (c) 2014 Mario Pastorelli (pastorelli.mario@gmail.com)
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
import shapeless.examples.StringConverter

import scala.collection.immutable.{:: => Cons}
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

  implicit def stringCSVConverter: StringConverter[String] =
    new StringConverter[String] {
      def from(s: String): Try[String] = Success(s)
      def to(s: String): String = s
    }

  implicit def intCsvConverter: StringConverter[Int] =
    new StringConverter[Int] {
      def from(s: String): Try[Int] = Try(s.toInt)
      def to(i: Int): String = i.toString
    }

  def listCsvLinesConverter[A](l: List[String])(implicit ec: StringConverter[A]): Try[List[A]] =
    l match {
      case Nil => Success(Nil)
      case Cons(s, ss) => for {
        x <- ec.from(s)
        xs <- listCsvLinesConverter(ss)(ec)
      } yield Cons(x, xs)
    }

  implicit def listCsvConverter[A](implicit ec: StringConverter[A]): StringConverter[List[A]] =
    new StringConverter[List[A]] {
      def from(s: String): Try[List[A]] = listCsvLinesConverter(s.split("\n").toList)(ec)
      def to(l: List[A]): String = l.map(ec.to).mkString("\n")
    }

  // HList
  implicit def deriveHNil: SeqConverter[HNil] =
    new SeqConverter[HNil] {
      def from(s: Seq[String]): Try[HNil] = s match {
        case Nil => Success(HNil)
        case s => fail("Cannot convert '" ++ s.toString ++ "' to HNil")
      }
    }

  // HList
  implicit def deriveHConsFromSeq[V, T <: HList](implicit scv: Lazy[StringConverter[V]], sct: Lazy[SeqConverter[T]]): SeqConverter[V :: T] =
    new SeqConverter[V :: T] {
      def from(s: Seq[String]): Try[V :: T] = {
        println("deriveHConsFromSeq")
        (s.head, s.tail) match {
          case (before, after) =>
            for {
              front <- scv.value.from(before)
              back <- sct.value.from(if (after.isEmpty) Nil else after.tail)
            } yield front :: back

          case _ => fail("Cannot convert '" ++ s.mkString("[",",","]") ++ "' to HList")
        }
      }
    }


  implicit def deriveClassFromSeq[A, R](implicit gen: Generic.Aux[A, R], toHListConv: SeqConverter[R]): SeqConverter[A] =
    new SeqConverter[A] {
      def from(s: Seq[String]): Try[A] = {
        println("deriveClassFromSeq")
        toHListConv.from(s).map(gen.from)
      }
    }
}