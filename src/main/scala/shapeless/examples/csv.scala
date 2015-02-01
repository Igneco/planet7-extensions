package shapeless.examples

/*
 * Copyright (c) 2014 Mario Pastorelli (pastorelli.mario@gmail.com)
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

import shapeless._, syntax.singleton._

import scala.collection.immutable.{:: => Cons}
import scala.util.{Try,Success,Failure}

class CSVException(s: String) extends RuntimeException

trait StringConverter[T] {
  def from(s: String): Try[T]
  def to(t: T): String
}

object StringConverter {
  def apply[T](implicit st: Lazy[StringConverter[T]]): StringConverter[T] = st.value

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
  implicit def deriveHNil: StringConverter[HNil] =
    new StringConverter[HNil] {
      def from(s: String): Try[HNil] = s match {
        case "" => Success(HNil)
        case s => fail("Cannot convert '" ++ s ++ "' to HNil")
      }

      def to(n: HNil) = ""
    }

  // HList
  implicit def deriveHCons[V, T <: HList](implicit scv: Lazy[StringConverter[V]], sct: Lazy[StringConverter[T]]): StringConverter[V :: T] =
    new StringConverter[V :: T] {
      def from(s: String): Try[V :: T] = s.span(_ != ',') match {
        case (before, after) =>
          for {
            front <- scv.value.from(before)
            back <- sct.value.from(if (after.isEmpty) after else after.tail)
          } yield front :: back

        case _ => fail("Cannot convert '" ++ s ++ "' to HList")
      }

      def to(ft: V :: T): String = {
        scv.value.to(ft.head) ++ "," ++ sct.value.to(ft.tail)
      }
    }

  implicit def deriveClass[A,R](implicit gen: Generic.Aux[A,R], toHListConv: StringConverter[R]): StringConverter[A] =
    new StringConverter[A] {
      def from(s: String): Try[A] = toHListConv.from(s).map(gen.from)
      def to(a: A): String = toHListConv.to(gen.to(a))
    }
}