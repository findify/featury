package io.findify.featury.model

import io.findify.featury.model.FeatureConfig._
import io.findify.featury.model.PeriodicCounterValue.PeriodicValue
import io.findify.featury.model.Write._

import scala.util.Random

sealed trait Feature[W <: Write, T <: FeatureValue, C <: FeatureConfig] {
  def put(action: W): Unit
  def config: C
  def computeValue(key: Key, ts: Timestamp): Option[T]
}

object Feature {
  trait ScalarFeature[T <: Scalar] extends Feature[Put[T], ScalarValue[T], ScalarConfig] {
    def makeValue(key: Key, ts: Timestamp, value: T): ScalarValue[T]
  }

  trait Counter extends Feature[Increment, LongScalarValue, CounterConfig]

  trait BoundedList[T <: Scalar] extends Feature[Append[T], BoundedListValue[T], BoundedListConfig]

  trait FreqEstimator extends Feature[PutFreqSample, FrequencyValue, FreqEstimatorConfig] {
    override final def put(action: PutFreqSample): Unit =
      if (Feature.shouldSample(config.sampleRate)) putSampled(action)
    def putSampled(action: PutFreqSample): Unit
  }

  trait PeriodicCounter extends Feature[PeriodicIncrement, PeriodicCounterValue, PeriodicCounterConfig] {
    def fromMap(map: Map[Timestamp, Long]): List[PeriodicValue] = {
      for {
        range         <- config.sumPeriodRanges
        lastTimestamp <- map.keys.toList.sortBy(_.ts).lastOption
      } yield {
        val start = lastTimestamp.minus(range.startOffset * config.period)
        val end   = lastTimestamp.minus(range.endOffset * config.period).plus(config.period)
        val sum =
          map.filterKeys(ts => ts.isBeforeOrEquals(end) && ts.isAfterOrEquals(start)).values.toList match {
            case Nil      => 0.0
            case nonEmpty => nonEmpty.sum
          }
        PeriodicValue(start, end, range.startOffset - range.endOffset + 1, sum)
      }
    }
  }

  trait StatsEstimator extends Feature[PutStatSample, NumStatsValue, StatsEstimatorConfig] {
    override final def put(action: PutStatSample): Unit =
      if (Feature.shouldSample(config.sampleRate)) putSampled(action)
    def putSampled(action: PutStatSample): Unit

  }

  def shouldSample(rate: Int): Boolean = Random.nextInt(rate) == 0
}
