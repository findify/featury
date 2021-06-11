package io.findify.featury.model

sealed trait ScalarType

object ScalarType {
  case object NumType  extends ScalarType
  case object TextType extends ScalarType
}
