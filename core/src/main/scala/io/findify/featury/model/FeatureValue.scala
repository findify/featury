package io.findify.featury.model

sealed trait FeatureValue {}

object FeatureValue {

  case class ScalarValue[T <: Scalar](value: T) extends FeatureValue
  object ScalarValue {
    def apply(value: String): ScalarValue[Text] = ScalarValue[Text](Text(value))
    def apply(value: Double): ScalarValue[Num]  = ScalarValue[Num](Num(value))
  }
  case class NumStatsValue(min: Double, max: Double, quantiles: Map[Int, Double]) extends FeatureValue
  case class PeriodicCounterValue(values: List[PeriodicValue])                    extends FeatureValue
  case class FrequencyValue(values: Map[String, Double])                          extends FeatureValue
  case class BoundedListValue[T <: Scalar](value: List[ListItem[T]])              extends FeatureValue

  case class PeriodicValue(start: Timestamp, end: Timestamp, periods: Int, value: Double)

  case class ListItem[T <: Scalar](value: T, ts: Timestamp)

  sealed trait Scalar
  case class Num(value: Double)  extends Scalar
  case class Text(value: String) extends Scalar

  sealed trait ScalarType
  case object NumType  extends ScalarType
  case object TextType extends ScalarType
}
