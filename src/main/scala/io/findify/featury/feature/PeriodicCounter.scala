package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.feature.Feature.State
import io.findify.featury.feature.PeriodicCounter.{PeriodicCounterConfig, PeriodicCounterState}
import io.findify.featury.model.FeatureValue.{Num, NumScalarValue, PeriodicNumValue, PeriodicValue}
import io.findify.featury.model.{Key, Timestamp}

import java.util
import scala.concurrent.duration.FiniteDuration

trait PeriodicCounter extends Feature[PeriodicCounterState, PeriodicNumValue] {
  def config: PeriodicCounterConfig
  def increment(key: Key, ts: Timestamp, value: Double): IO[Unit]
  override def empty(): PeriodicCounterState = PeriodicCounterState(Timestamp(0L))
  override def computeValue(state: PeriodicCounterState): PeriodicNumValue = {
    val result = for {
      range <- config.sumPeriodRanges
    } yield {
      val start = state.now.toStartOfPeriod(config.period).minus(range.startOffset * config.period)
      val end   = state.now.minus(range.endOffset * config.period)
      val sum =
        state.periods.filterKeys(ts => ts.isBeforeOrEquals(end) && ts.isAfterOrEquals(start)).values.toList match {
          case Nil      => 0.0
          case nonEmpty => nonEmpty.sum
        }
      PeriodicValue(start, end, range.startOffset - range.endOffset + 1, sum)
    }
    PeriodicNumValue(result)
  }

}

object PeriodicCounter {
  case class RangeCount(start: Timestamp, count: Double)
  case class PeriodicCounterState(now: Timestamp, periods: Map[Timestamp, Double] = Map.empty) extends State
  case class PeriodicCounterConfig(period: FiniteDuration, count: Int, sumPeriodRanges: List[PeriodRange])
  case class PeriodRange(startOffset: Int, endOffset: Int)

}
