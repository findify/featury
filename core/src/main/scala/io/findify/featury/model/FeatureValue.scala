package io.findify.featury.model

sealed trait FeatureValue {}

object FeatureValue {

  sealed trait ScalarValue[T <: Scalar] extends FeatureValue {
    def value: T
  }
  object ScalarValue {
    case class StringScalarValue(value: SString) extends ScalarValue[SString]
    case class DoubleScalarValue(value: SDouble) extends ScalarValue[SDouble]
    case class LongScalarValue(value: SLong)     extends ScalarValue[SLong]
    def apply(value: String): ScalarValue[SString] = StringScalarValue(SString(value))
    def apply(value: Long): ScalarValue[SLong]     = LongScalarValue(SLong(value))
    def apply(value: Double): ScalarValue[SDouble] = DoubleScalarValue(SDouble(value))
  }
  case class NumStatsValue(min: Double, max: Double, quantiles: Map[Int, Double]) extends FeatureValue
  case class PeriodicCounterValue(values: List[PeriodicValue])                    extends FeatureValue
  case class FrequencyValue(values: Map[String, Double])                          extends FeatureValue
  case class BoundedListValue[T <: Scalar](value: List[ListItem[T]])              extends FeatureValue

  case class PeriodicValue(start: Timestamp, end: Timestamp, periods: Int, value: Double)

  case class ListItem[T <: Scalar](value: T, ts: Timestamp)

//  sealed trait Scalar               extends Any
//  case class SLong(value: Long)     extends AnyVal with Scalar
//  case class SDouble(value: Double) extends AnyVal with Scalar
//  case class SString(value: String) extends AnyVal with Scalar

  sealed trait ScalarType
  case object NumType  extends ScalarType
  case object TextType extends ScalarType
}
