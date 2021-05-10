package io.findify.featury.model

sealed trait FeatureValue {}

object FeatureValue {
  type NumValue  = ScalarValue[Num]
  type TextValue = ScalarValue[Text]

  case class ScalarValue[T <: Scalar](value: T)                                      extends FeatureValue
  case class NumStatsValue(min: Double, max: Double, quantiles: Map[Double, Double]) extends FeatureValue
  case class PeriodicNumValue(values: Map[Long, Double])                             extends FeatureValue
  case class StringFrequencyValue(values: Map[String, Double])                       extends FeatureValue
  case class BoundedListValue[T <: Scalar](values: List[ListItem[T]])                extends FeatureValue

  case class ListItem[T <: Scalar](value: T, ts: Timestamp)

  sealed trait Scalar
  case class Num(value: Double)  extends Scalar
  case class Text(value: String) extends Scalar

  sealed trait ScalarType
  case object NumType  extends ScalarType
  case object TextType extends ScalarType
}
