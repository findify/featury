package io.findify.featury.model

sealed trait FeatureValue {}

object FeatureValue {

  sealed trait ScalarValue[T <: Scalar] extends FeatureValue {
    def value: T
  }
  case class TextScalarValue(value: Text)                                            extends ScalarValue[Text]
  case class NumScalarValue(value: Num)                                              extends ScalarValue[Num]
  case class NumStatsValue(min: Double, max: Double, quantiles: Map[Double, Double]) extends FeatureValue
  case class PeriodicNumValue(values: List[PeriodicValue])                           extends FeatureValue
  case class StringFrequencyValue(values: Map[String, Double])                       extends FeatureValue
  sealed trait BoundedListValue[T <: Scalar] extends FeatureValue {
    def values: List[ListItem[T]]
  }
  case class TextBoundedListValue(values: List[ListItem[Text]]) extends BoundedListValue[Text]
  case class NumBoundedListValue(values: List[ListItem[Num]])   extends BoundedListValue[Num]

  case class PeriodicValue(start: Timestamp, end: Timestamp, periods: Int, value: Double)

  case class ListItem[T <: Scalar](value: T, ts: Timestamp)

  sealed trait Scalar
  case class Num(value: Double)  extends Scalar
  case class Text(value: String) extends Scalar

  sealed trait ScalarType
  case object NumType  extends ScalarType
  case object TextType extends ScalarType
}
