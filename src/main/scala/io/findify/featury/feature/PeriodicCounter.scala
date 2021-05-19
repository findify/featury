package io.findify.featury.feature

import cats.data.NonEmptyList
import cats.effect.IO
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.feature.Feature.State
import io.findify.featury.feature.PeriodicCounter.{PeriodicCounterConfig, PeriodicCounterState}
import io.findify.featury.model.FeatureValue.{Num, NumScalarValue, PeriodicNumValue, PeriodicValue}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Schema.FeatureConfig
import io.findify.featury.model.{Key, Timestamp}

import java.util
import scala.concurrent.duration.FiniteDuration

trait PeriodicCounter extends Feature[PeriodicCounterState, PeriodicNumValue, PeriodicCounterConfig] {
  def config: PeriodicCounterConfig
  def increment(key: Key, ts: Timestamp, value: Long): IO[Unit]
  override def computeValue(state: PeriodicCounterState): Option[PeriodicNumValue] = {
    val result = for {
      range         <- config.sumPeriodRanges.toList
      lastTimestamp <- state.periods.keys.toList.sortBy(_.ts).lastOption
    } yield {
      val start = lastTimestamp.minus(range.startOffset * config.period)
      val end   = lastTimestamp.minus(range.endOffset * config.period).plus(config.period)
      val sum =
        state.periods.filterKeys(ts => ts.isBeforeOrEquals(end) && ts.isAfterOrEquals(start)).values.toList match {
          case Nil      => 0.0
          case nonEmpty => nonEmpty.sum
        }
      PeriodicValue(start, end, range.startOffset - range.endOffset + 1, sum)
    }
    Some(PeriodicNumValue(result))
  }

}

object PeriodicCounter {
  case class RangeCount(start: Timestamp, count: Double)
  case class PeriodicCounterState(periods: Map[Timestamp, Long] = Map.empty) extends State
  case class PeriodicCounterConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      period: FiniteDuration,
      count: Int,
      sumPeriodRanges: NonEmptyList[PeriodRange]
  ) extends FeatureConfig {
    private val periods = sumPeriodRanges
      .map(_.startOffset)
      .concatNel(sumPeriodRanges.map(_.endOffset))
      .sorted
    val latestPeriodOffset   = periods.head
    val earliestPeriodOffset = periods.last
  }
  case class PeriodRange(startOffset: Int, endOffset: Int)

}
