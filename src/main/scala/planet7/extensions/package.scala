package planet7

import shapeless.Lazy

package object extensions
  extends SeqConverterImplicits
  with RowConverterImplicits
  with MonoidImplicits {

  object ConvertTo {
    def apply[T](implicit rowConv: Lazy[RowConverter[T]]): RowConverter[T] = rowConv.value
  }

  object ConvertFrom {
    def apply[T](implicit rowConv: Lazy[RowConverter[T]]): RowConverter[T] = rowConv.value
  }
}