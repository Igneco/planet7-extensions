package planet7

package object extensions {
  type StringConverter[T] = shapeless.examples.CSVConverter[T]
//  type SeqConverter[T] = planet7.extensions.SeqConverter[T]
  import shapeless.examples.CSVConverter
  import planet7.extensions.SeqConverter
}
